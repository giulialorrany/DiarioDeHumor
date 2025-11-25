package org.example.diariodehumor.controller;

import org.example.diariodehumor.dao.HumorDAO;
import org.example.diariodehumor.model.AnalysisDTO;
import org.example.diariodehumor.model.HumorDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/humor")
public class HumorController {

    private static final HumorDAO dao = new HumorDAO();

    @GetMapping("/calendar")
    public List<HumorDTO> selectCalendar(@RequestParam int month, @RequestParam int year) {
        return dao.getCalendar(month, year);
    }

    @GetMapping("/analysis")
    public AnalysisDTO selectAnalysis(@RequestParam String period, @RequestParam String day) {
        return dao.getAnalysis(period, day);
    }

    @GetMapping("/streak")
    public int selectStreak(@RequestParam String day) {
        return dao.getStreak(day);
    }

    // Request method = POST
    @PostMapping
    public String save(@RequestBody HumorDTO entry) {
        dao.save(entry);
        return "OK";
    }
}
