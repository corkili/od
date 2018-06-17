package org.ljk.od;

import java.beans.IntrospectionException;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.persistence.criteria.Predicate;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.thymeleaf.util.StringUtils;

@Controller
public class ODController {

    private static final Logger LOG = LoggerFactory.getLogger(ODController.class);

    @Autowired
    ODRepository repository;

    private String message = "";

    private List<OpticalDevice> opticalDevices;

    private boolean isQuery = false;
    private Predicate lastPredicate;

    @RequestMapping("/")
    public String index(Model model) {
        if (!isQuery) {
            opticalDevices = repository.findAll();
        }
        model.addAttribute("ods", opticalDevices);
        model.addAttribute("od", new OpticalDevice());
        model.addAttribute("message", new String(message.getBytes(Charset.forName("UTF-8")), Charset.forName("UTF-8")));
        message = "";
        return "index";
    }

    @RequestMapping("/all")
    public String showAll() {
        isQuery = false;
        return "redirect:/";
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveOrUpdate(@ModelAttribute("od") OpticalDevice od) {
        LOG.info("received od information: {}", od);
        boolean isSave = od.getId() == null;
        if (checkOd(od)) {
            if (repository.save(od).getId() == null) {
                message = (isSave ? "添加" : "更新") + "光器件信息失败";
            }
        }
        if (isQuery) {
            innerQuery();
        }
        return "redirect:/";
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public String saveOrUpdate(@RequestParam("id") Long id) {
        LOG.info("delete id: {}", id);
        repository.deleteById(id);
        if (isQuery) {
            innerQuery();
        }
        return "redirect:/";
    }

    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public String query(@RequestParam("query") String query) {
        LOG.info("query : {}", query);
        innerQuery(query);
        return "redirect:/";
    }

    @RequestMapping(value="uploadExcel",method={RequestMethod.POST})
    public String uploadExcel(HttpServletRequest request) throws Exception {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile file = multipartRequest.getFile("upfile");
        if(file.isEmpty()){
            message = "文件不存在!";
            return "redirect:/";
        }
        InputStream in = file.getInputStream();
        List<OpticalDevice> ods = getOdsByExcel(in, file);
        in.close();
        List<OpticalDevice> result = repository.saveAll(ods);
        long count = result.stream().filter(opticalDevice -> opticalDevice.getId() != null).count();
        if (count == 0) {
            message = "未导入任何数据";
        } else {
            message = "共导入" + count + "条数据";
        }
        return "redirect:/";
    }

    @RequestMapping(value = "/downloadExcel", method = RequestMethod.GET)
    @ResponseBody
    public void downloadExcel(HttpServletResponse response){
        response.reset();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:ms");
        String dateStr = sdf.format(new Date());
        // 指定下载的文件名
        response.setHeader("Content-Disposition", "attachment;filename=" + dateStr + ".xlsx");
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        XSSFWorkbook workbook=null;
        try {
            //导出Excel对象
            workbook = exportExcel();
        } catch (IllegalArgumentException | IllegalAccessException
                | InvocationTargetException | ClassNotFoundException
                | IntrospectionException | ParseException e) {
            e.printStackTrace();
        }
        if (workbook != null) {
            OutputStream output;
            try {
                output = response.getOutputStream();
                BufferedOutputStream bufferedOutPut = new BufferedOutputStream(output);
                bufferedOutPut.flush();
                workbook.write(bufferedOutPut);
                bufferedOutPut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void innerQuery(String query) {
        final JSONObject q = JSON.parseObject(query);
        opticalDevices = repository.findAll((Specification<OpticalDevice>) (root, criteriaQuery, cb) -> {
            Set<String> usedCondition = new HashSet<>();
            Set<String> waitProcessCond = new HashSet<>(q.keySet());
            Set<String> processedCond = new HashSet<>();
            Map<String, Predicate> conditions = new LinkedHashMap<>();

            // 处理条件
            while (waitProcessCond.size() > 0) {
                waitProcessCond.forEach(conditionId -> {
                    JSONObject conditionEntity = q.getJSONObject(conditionId);
                    int type = conditionEntity.getInteger("type");
                    String op = conditionEntity.getString("op");
                    String key = conditionEntity.getString("key");
                    if (type == 1 && "like".equalsIgnoreCase(op)) {
                        conditions.put(conditionId, cb.like(
                                root.get(key).as(String.class),
                                "%" + conditionEntity.getString("value") + "%"));
                        processedCond.add(conditionId);
                    } else if (type == 1 && "=".equals(op)) {
                        conditions.put(conditionId, cb.equal(
                                root.get(key).as(String.class),
                                conditionEntity.getString("value")));
                        processedCond.add(conditionId);
                    } else if (type == 2 && "=".equals(op)) {
                        conditions.put(conditionId, cb.equal(
                                root.get(key).as(Double.class),
                                conditionEntity.getDouble("value")));
                        processedCond.add(conditionId);
                    } else if (type == 2 && "<>".equals(op)) {
                        conditions.put(conditionId, cb.notEqual(
                                root.get(key).as(Double.class),
                                conditionEntity.getDouble("value")));
                        processedCond.add(conditionId);
                    } else if (type == 2 && ">".equals(op)) {
                        conditions.put(conditionId, cb.greaterThan(
                                root.get(key).as(Double.class),
                                conditionEntity.getDouble("value")));
                        processedCond.add(conditionId);
                    } else if (type == 2 && ">=".equals(op)) {
                        conditions.put(conditionId, cb.greaterThanOrEqualTo(
                                root.get(key).as(Double.class),
                                conditionEntity.getDouble("value")));
                        processedCond.add(conditionId);
                    } else if (type == 2 && "<".equals(op)) {
                        conditions.put(conditionId, cb.lessThan(
                                root.get(key).as(Double.class),
                                conditionEntity.getDouble("value")));
                        processedCond.add(conditionId);
                    } else if (type == 2 && "<=".equals(op)) {
                        conditions.put(conditionId, cb.lessThanOrEqualTo(
                                root.get(key).as(Double.class),
                                conditionEntity.getDouble("value")));
                        processedCond.add(conditionId);
                    } else if (type == 3  && conditions.containsKey(key) && "not".equalsIgnoreCase(op)) {
                        conditions.put(conditionId, cb.not(conditions.get(key)));
                        processedCond.add(conditionId);
                        usedCondition.add(key);
                    } else if (type == 4 && conditions.containsKey(key) && "and".equalsIgnoreCase(op)) {
                        String value = conditionEntity.getString("value");
                        if (conditions.containsKey(value)) {
                            conditions.put(conditionId, cb.and(conditions.get(key), conditions.get(value)));
                            processedCond.add(conditionId);
                            usedCondition.addAll(Arrays.asList(key, value));
                        }
                    } else if (type == 4 && conditions.containsKey(key) && "or".equalsIgnoreCase(op)) {
                        String value = conditionEntity.getString("value");
                        if (conditions.containsKey(value)) {
                            conditions.put(conditionId, cb.or(conditions.get(key), conditions.get(value)));
                            processedCond.add(conditionId);
                            usedCondition.addAll(Arrays.asList(key, value));
                        }
                    }
                });
                waitProcessCond.removeAll(processedCond);
                processedCond.clear();
            }

            List<Predicate> predicates = new ArrayList<>();
            conditions.forEach((key, value) -> {
                if (!usedCondition.contains(key)) {
                    predicates.add(value);
                }
            });
            Predicate[] p = new Predicate[predicates.size()];
            Predicate predicate = cb.and(predicates.toArray(p));
            setLastPredicate(predicate);
            return predicate;
        });
        isQuery = true;
    }

    private void innerQuery() {
        opticalDevices = repository.findAll(
                (Specification<OpticalDevice>) (root, criteriaQuery, criteriaBuilder) -> lastPredicate);
    }

    private void setLastPredicate(Predicate predicate) {
        this.lastPredicate = predicate;
    }

    private List<OpticalDevice> getOdsByExcel(InputStream in, MultipartFile file) throws Exception {
        List<List<Object>> listob = ExcelUtils.getListByExcel(in,file.getOriginalFilename());
        List<OpticalDevice> ods = new ArrayList<>();
        for (List<Object> ob : listob) {
            OpticalDevice opticalDevice = new OpticalDevice();
            opticalDevice.setName(String.valueOf(ob.get(1)));
            opticalDevice.setType(String.valueOf(ob.get(2)));
            opticalDevice.setFirstCategory(String.valueOf(ob.get(3)));
            opticalDevice.setSecondCategory(String.valueOf(ob.get(4)));
            BigDecimal price = new BigDecimal(String.valueOf(ob.get(5)));
            opticalDevice.setPrice(price.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            opticalDevice.setFactory(String.valueOf(ob.get(6)));
            opticalDevice.setCharacteristics(String.valueOf(ob.get(7)));
            opticalDevice.setDescribe(String.valueOf(ob.get(8)));
            if (checkOd(opticalDevice)) {
                ods.add(opticalDevice);
            } else {
                LOG.error("{} : {}", message, opticalDevice);
            }
        }
        message = "";
        LOG.info("size: {}, {}", ods.size(), ods);
        return ods;
    }

    private XSSFWorkbook exportExcel() throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, ClassNotFoundException, IntrospectionException, ParseException {
        List<ExcelBean> ems = new ArrayList<>();
        Map<Integer,List<ExcelBean>> map = new LinkedHashMap<>();
        XSSFWorkbook book;
        ems.add(new ExcelBean("ID","id",0));
        ems.add(new ExcelBean("名称","name",0));
        ems.add(new ExcelBean("规格","type",0));
        ems.add(new ExcelBean("一级类别","firstCategory",0));
        ems.add(new ExcelBean("二级类别","secondCategory",0));
        ems.add(new ExcelBean("价格","price",0));
        ems.add(new ExcelBean("代表厂家","factory",0));
        ems.add(new ExcelBean("性能特点", "characteristics", 0));
        ems.add(new ExcelBean("产品描述", "describe", 0));
        map.put(0, ems);
        book = ExcelUtils.createExcelFile(OpticalDevice.class, opticalDevices, map, "光器件信息");
        return book;
    }

    private boolean checkOd(OpticalDevice od) {
        if (StringUtils.isEmptyOrWhitespace(od.getName())) {
            message = "器件名称不能为空";
            return false;
        }
        if (StringUtils.isEmptyOrWhitespace(od.getType())) {
            message = "规格不能为空";
            return false;
        }
        if (StringUtils.isEmptyOrWhitespace(od.getFirstCategory())) {
            message = "一级类别不能为空";
            return false;
        }
        if (StringUtils.isEmptyOrWhitespace(od.getSecondCategory())) {
            message = "二级类别不能为空";
            return false;
        }
        if (StringUtils.isEmptyOrWhitespace(od.getFactory())) {
            message = "代表厂家不能为空";
            return false;
        }
        if (StringUtils.isEmptyOrWhitespace(od.getCharacteristics())) {
            message = "性能特点不能为空";
            return false;
        }
        if (StringUtils.isEmptyOrWhitespace(od.getDescribe())) {
            message = "产品描述不能为空";
            return false;
        }
        if (od.getPrice() == null || od.getPrice() <= 0) {
            od.setPrice(0.0);
        }
        return true;
    }

}
