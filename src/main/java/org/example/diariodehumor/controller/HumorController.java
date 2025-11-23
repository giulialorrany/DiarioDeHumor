package org.example.diariodehumor.controller;

import org.example.diariodehumor.dao.HumorDAO;
import org.example.diariodehumor.model.HumorDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/humor")
public class HumorController {

    private static final HumorDAO dao = new HumorDAO();

    // Request method = GET
    @GetMapping("/calendar")
    public List<HumorDTO> selectCalendar(@RequestParam int month, @RequestParam int year) {
        return dao.selectByCurrentMonth(month, year);
    }

    // Request method = GET
    @GetMapping("/analysis")
    public List<HumorDTO> selectAnalysis(@RequestParam String period, @RequestParam String day) {
        return dao.analysis(period, day);
    }

    // Request method = POST
    @PostMapping
    public String save(@RequestBody HumorDTO entry) {
        dao.save(entry);
        return "OK";
    }
}
