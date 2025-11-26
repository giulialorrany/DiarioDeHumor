package org.example.diariodehumor.dao;

import org.example.diariodehumor.model.AnalysisDTO;
import org.example.diariodehumor.model.HumorDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // permite usar @BeforeAll sem static
class HumorDAOTest {

    @Autowired
    private HumorDAO dao;

    // Limpa tudo antes de cada teste
    @BeforeEach
    void clean() {
        // Apaga registros de teste que usamos nas datas fixas
        dao.delete(new HumorDTO("Mon Oct 23 2023", null, null));
        dao.delete(new HumorDTO("Tue Oct 24 2023", null, null));
        dao.delete(new HumorDTO("Wed Oct 25 2023", null, null));
    }

    @Test
    void testSaveCreate() {
        HumorDTO dto = new HumorDTO("Wed Oct 25 2023", "good", "Feeling good");

        dao.save(dto);

        HumorDTO result = dao.selectByDay("Wed Oct 25 2023");
        assertThat(result, notNullValue());
        assertThat(result.getMood(), equalTo("good"));
        assertThat(result.getNote(), equalTo("Feeling good"));
    }

    @Test
    void testSaveUpdate() {
        // cria primeiro
        dao.save(new HumorDTO("Mon Oct 25 2023", "good", "Normal day"));

        // atualiza
        HumorDTO dto = new HumorDTO("Mon Oct 25 2023", "excellent", "Best day ever!");
        dao.save(dto);

        HumorDTO result = dao.selectByDay("Mon Oct 25 2023");
        assertThat(result.getMood(), equalTo("excellent"));
        assertThat(result.getNote(), equalTo("Best day ever!"));
    }

    @Test
    void testDelete() {
        dao.save(new HumorDTO("Mon Oct 25 2023", "bad", "Bad day"));

        dao.delete(new HumorDTO("Mon Oct 25 2023", null, null));

        assertThat(dao.selectByDay("Mon Oct 25 2023"), nullValue());
    }

    @Test
    void testGetStreak() {
        dao.save(new HumorDTO("Mon Oct 23 2023", "good", ""));
        dao.save(new HumorDTO("Tue Oct 24 2023", "good", ""));
        dao.save(new HumorDTO("Wed Oct 25 2023", "excellent", ""));

        int streak = dao.getStreak("Wed Oct 25 2023");

        assertThat(streak, equalTo(3));
    }

    @Test
    void testGetAnalysis() {
        dao.save(new HumorDTO("Mon Oct 23 2023", "good", null));
        dao.save(new HumorDTO("Tue Oct 24 2023", "excellent", null));
        dao.save(new HumorDTO("Wed Oct 25 2023", "ok", null));

        AnalysisDTO analysis = dao.getAnalysis("week", "Wed Oct 25 2023");

        assertThat(analysis.getTotalDays(), equalTo(3));
        assertThat(analysis.getBestMood(), equalTo("excellent"));
        assertThat(analysis.getMoodAvg(), closeTo(3.0, 0.4)); // (3 + 4 + 2)/3 = 9/3 = 3.0
        // aceitar margem de erro de 0.4 por causa do ponto flutuante
    }
}