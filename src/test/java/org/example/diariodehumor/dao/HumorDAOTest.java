package org.example.diariodehumor.dao;

import org.example.diariodehumor.model.AnalysisDTO;
import org.example.diariodehumor.model.HumorDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest // Anotação para poder rodar com o Spring Boot
class HumorDAOTest {

    @Autowired
    private HumorDAO dao;

    // Limpa os dados antes de cada teste (usa banco de dados h2)
    @BeforeEach
    void clean() {
        // Apaga registros de teste nessas datas fixas
        dao.delete(new HumorDTO("Mon Oct 23 2023", null, null));
        dao.delete(new HumorDTO("Tue Oct 24 2023", null, null));
        dao.delete(new HumorDTO("Wed Oct 25 2023", null, null));
    }

    @Test
    void testCreate() {
        HumorDTO dto = new HumorDTO("Wed Oct 25 2023", "good", "Feeling good");

        // testa antes de criar
        HumorDTO result = dao.selectByDay("Wed Oct 25 2023");
        assertThat(result, nullValue());

        // cria
        dao.save(dto);

        // testa depois de criar
        result = dao.selectByDay("Wed Oct 25 2023");
        assertThat(result, notNullValue());
        assertThat(result.getMood(), equalTo("good"));
        assertThat(result.getNote(), equalTo("Feeling good"));
    }

    @Test
    void testUpdate() {
        // cria primeiro
        dao.save(new HumorDTO("Mon Oct 25 2023", "good", "Normal day"));

        // atualiza
        HumorDTO dto = new HumorDTO("Mon Oct 25 2023", "excellent", "Best day ever!");
        dao.save(dto);

        // testa
        HumorDTO result = dao.selectByDay("Mon Oct 25 2023");
        assertThat(result.getMood(), equalTo("excellent"));
        assertThat(result.getNote(), equalTo("Best day ever!"));
    }

    @Test
    void testDelete() {
        HumorDTO dto = new HumorDTO("Mon Oct 25 2023", "bad", "Bad day");

        // cria primeiro
        dao.save(dto);
        assertThat(dao.selectByDay("Mon Oct 25 2023"), notNullValue());

        // deleta
        dao.delete(dto);
        assertThat(dao.selectByDay("Mon Oct 25 2023"), nullValue());
    }

    @Test
    void testGetStreak() {
        // testa se retorna o número de dias consecutivos
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

        // total de dias registrados
        assertThat(analysis.getTotalDays(), equalTo(3));

        // melhor humor
        assertThat(analysis.getBestMood(), equalTo("excellent"));

        // média do humor
        assertThat(analysis.getMoodAvg(), closeTo(3.0, 0.4));
        // aceitar margem de erro de 0.4 por causa do ponto flutuante

        // Pesos:
        // terrible = 0
        // bad = 1
        // ok = 2
        // good = 3
        // excellent = 4

        // (3 + 4 + 2)/3 = 9/3 = 3.0
    }
}