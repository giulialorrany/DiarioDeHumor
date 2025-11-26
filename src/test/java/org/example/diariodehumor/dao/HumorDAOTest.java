package org.example.diariodehumor.dao;

import org.example.diariodehumor.model.HumorDTO;
import org.example.diariodehumor.model.AnalysisDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class HumorDAOTest {

    private HumorDAO dao;
    private HumorDTO dto;

    @BeforeEach
    void setUp() {
        // Inicializar o DAO e a entrada de exemplo
        dao = new HumorDAO();
        dto = new HumorDTO();
        dto.setDate("Mon Oct 25 2023");
        dto.setMood("good");
        dto.setNote("Feeling good today");
    }

    @Test
    void testSaveCreate() {
        dao.save(dto); // Deve criar o registro
        HumorDTO result = dao.selectByDay(dto.getDate());

        assertNotNull(result);
        assertThat(result.getMood(), equalTo("good"));
        assertThat(result.getNote(), equalTo("Feeling good today"));
    }

    @Test
    void testSaveUpdate() {
        // Cria o primeiro registro
        dao.save(dto);

        // Altera o humor e salva novamente
        dto.setMood("excellent");
        dto.setNote("Fantastic day!");
        dao.save(dto);

        HumorDTO result = dao.selectByDay(dto.getDate());
        assertNotNull(result);
        assertThat(result.getMood(), equalTo("excellent"));
        assertThat(result.getNote(), equalTo("Fantastic day!"));
    }

    @Test
    void testDelete() {
        dao.save(dto);
        dao.delete(dto);

        HumorDTO result = dao.selectByDay(dto.getDate());
        assertNull(result);
    }

    @Test
    void testSelectByDay() {
        dao.save(dto);
        HumorDTO result = dao.selectByDay(dto.getDate());

        assertNotNull(result);
        assertThat(result.getDate(), equalTo(dto.getDate()));
        assertThat(result.getMood(), equalTo("good"));
        assertThat(result.getNote(), equalTo("Feeling good today"));
    }

    @Test
    void testGetAnalysisByWeek() {
        // Simular alguns registros para análise
        dto.setDate("Mon Oct 25 2023");
        dto.setMood("good");
        dao.save(dto);

        dto.setDate("Tue Oct 26 2023");
        dto.setMood("bad");
        dao.save(dto);

        String period = "week";
        String date = "Mon Oct 25 2023"; // Data de início
        AnalysisDTO analysis = dao.getAnalysis(period, date);

        assertNotNull(analysis);
        assertThat(analysis.getMoodAvg(), greaterThanOrEqualTo(0.0));
        assertThat(analysis.getBestMood(), equalTo("good"));
        assertThat(analysis.getTotalDays(), greaterThan(0));
    }

    @Test
    void testGetStreak() {
        // Simular um streak
        dto.setDate("Mon Oct 25 2023");
        dto.setMood("good");
        dao.save(dto);

        int streak = dao.getStreak(dto.getDate());
        assertThat(streak, equalTo(1));

        // Adicionar mais dias consecutivos e testar o streak
        dto.setDate("Tue Oct 26 2023");
        dao.save(dto);

        streak = dao.getStreak(dto.getDate());
        assertThat(streak, equalTo(2)); // Espera-se que o streak tenha crescido
    }
}
