package org.example.diariodehumor.controller;

import org.example.diariodehumor.dao.HumorDAO;
import org.example.diariodehumor.model.HumorDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/humor")
public class HumorController {

    private final HumorDAO dao;

    public HumorController(HumorDAO dao) {
        this.dao = dao;
    }

    // Request method = GET
    @GetMapping
    public List<HumorDTO> list() {
        return dao.selectAll();
    }

    // Request method = POST
    @PostMapping
    public String save(@RequestBody HumorDTO entry) {
        dao.save(entry);
        return "OK";
    }
}
