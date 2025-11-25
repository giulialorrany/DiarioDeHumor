package org.example.diariodehumor.dao;

import org.example.diariodehumor.model.Conexao;
import org.example.diariodehumor.model.HumorDTO;
import org.example.diariodehumor.model.AnalysisDTO;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Component
public class HumorDAO {

    Connection conn;
    PreparedStatement stmt;
    ResultSet rs;

    static final SimpleDateFormat frontDate = new SimpleDateFormat("E MMM dd yyyy", Locale.ENGLISH);
    static final SimpleDateFormat backDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);


    // -------------------- CREATE / UPDATE / DELETE --------------------
    // SAVE (CREATE || UPDATE)
    public void save(HumorDTO entry) {
        System.out.println("método save() chamado!");

        if (entry.getDate() != null && selectByDay(entry.getDate()) != null) {
            update(entry);
        } else {
            create(entry);
        }
    }

    // CREATE
    public void create(HumorDTO entry) {
        System.out.println("método create() chamado!");

        String sql = "INSERT INTO humor_dia (day_date, mood, note) VALUES (?, ?, ?)";

        conn = new Conexao().conectaBD();
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setDate(1, convertDate(entry.getDate()));
            stmt.setString(2, entry.getMood());
            stmt.setString(3, entry.getNote());
            stmt.execute();
            stmt.close();
        } catch (SQLException | ParseException e) {
            System.out.println("Erro "+ e.getClass().getSimpleName() +" em create(): " + e.getMessage());
        }
    }

    // UPDATE
    public void update(HumorDTO entry) {
        System.out.println("método update() chamado!");

        String sql = "UPDATE humor_dia SET mood=?, note=? WHERE day_date=?";
        conn = new Conexao().conectaBD();
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, entry.getMood());
            stmt.setString(2, entry.getNote());
            stmt.setDate(3, convertDate(entry.getDate()));
            stmt.execute();
            stmt.close();
        } catch (SQLException | ParseException e) {
            System.out.println("Erro "+ e.getClass().getSimpleName() +" em update(): " + e.getMessage());
        }
    }

    // DELETE
    public void delete(HumorDTO entry) {
        System.out.println("método delete() chamado!");

        String sql = "DELETE FROM humor_dia WHERE day_date=?";

        conn = new Conexao().conectaBD();
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setDate(1, convertDate(entry.getDate()));
            stmt.execute();
            stmt.close();
        } catch (SQLException | ParseException e) {
            System.out.println("Erro "+ e.getClass().getSimpleName() +" em delete(): " + e.getMessage());
        }
    }


    // -------------------- Full Query --------------------
    // Analysis
    public AnalysisDTO getAnalysis(String period, String date) {
        System.out.println("método getAnalysis() chamado!");
        Date dateC = new Date(System.currentTimeMillis());
        try {
            dateC = convertDate(date);
        } catch (ParseException e) {
            System.out.println("Erro "+ e.getClass().getSimpleName() +" em getAnalysis(): " + e.getMessage());
        }

        // Pega registros para análise
        List<HumorDTO> humorList =  switch (period) {
            case "week" -> selectByWeek(date);
            case "month" -> selectByMonth(date);
            default -> select2Weeks(date);
        };

        // Define o valor dos humores para encontrar o melhor e ajudar a criar a média
        Map<String,Integer> moodValue = new HashMap<>();
        moodValue.put("terrible", 0);
        moodValue.put("bad", 1);
        moodValue.put("ok", 2);
        moodValue.put("good", 3);
        moodValue.put("excellent", 4);

        // Conta quantidade de cada humor
        Map<String,Integer> moodCount = new HashMap<>();
        moodCount.put("terrible", 0);
        moodCount.put("bad", 0);
        moodCount.put("ok", 0);
        moodCount.put("good", 0);
        moodCount.put("excellent", 0);

        for(HumorDTO humor : humorList) {
            String mood = humor.getMood();
            moodCount.put(mood, moodCount.get(mood) + 1);
        }

        // Calcula a média e encontrar o melhor humor
        int totalDays = 0, moodSum = 0;
        String bestMood = "terrible";

        for(Map.Entry<String,Integer> entry : moodCount.entrySet()) {
            String mood = entry.getKey();
            int count = entry.getValue();

            // Atualiza o total de dias registrados
            totalDays += count;

            // Atualiza o somatório de valores de humor para a média
            moodSum += moodValue.get(mood) * count;

            // Atualiza o melhor humor
            if (count > 0 && moodValue.get(mood) > moodValue.get(bestMood)) {
                bestMood = mood;
            }
        }

        // Calcula a média
        double average = totalDays > 0 ? (double) moodSum / totalDays : 0;

        // Calcula a consistência
        int timePeriod = switch (period) {
            case "week" -> 7;
            case "biweekly" -> 14;
            default -> getDaysInMonth(dateC);
        };
        double consistency = (double) totalDays / timePeriod * 100;

        // Forma o Objeto "AnalysisDTO"
        AnalysisDTO analysis = new AnalysisDTO();
        analysis.setMoodAvg(average);
        analysis.setTotalDays(totalDays);
        analysis.setBestMood(bestMood);
        analysis.setConsistency(consistency);
        analysis.setMoodCount(moodCount);

        return analysis;
    }

    // Streak
    public int getStreak(String todayDate) {
        System.out.println("método getStreak() chamado!");
        Date today;

        try {
            today = convertDate(todayDate);
        } catch (ParseException e) {
            System.out.println("Erro em convertDate: " + e.getMessage());
            return 0;
        }

        String sql = """
            SELECT DISTINCT day_date
            FROM humor_dia
            WHERE day_date <= ?
            ORDER BY day_date DESC
        """;

        conn = new Conexao().conectaBD();

        List<Date> dates = new ArrayList<>();

        try {
            stmt = conn.prepareStatement(sql);
            stmt.setDate(1, today);
            rs = stmt.executeQuery();

            while (rs.next()) {
                dates.add(rs.getDate("day_date"));
            }

        } catch(SQLException e) {
            System.out.println("Erro SQL: " + e.getMessage());
            return 0;
        }

        if (dates.isEmpty())
            return 0;

        // O primeiro dia deve ser hoje para streak > 0
        if (!dates.get(0).equals(today))
            return 0;

        int streak = 1;

        for (int i = 1; i < dates.size(); i++) {
            Date expected = addDays(dates.get(i - 1), -1);
            if (dates.get(i).equals(expected)) {
                streak++;
            } else {
                break;
            }
        }

        return streak;
    }

    // Calendar
    public List<HumorDTO> getCalendar(int month, int year) {
        System.out.println("método getCalendar() chamado!");
        month++; // 0 indexed    js(new Date().getMonth())
        System.out.println("método selectByCurrentMonth() chamado!");

        String sql = "SELECT * " +
                "FROM humor_dia " +
                "WHERE MONTH(day_date)=? " +
                "AND YEAR(day_date)=?";
        conn = new Conexao().conectaBD();
        List<HumorDTO> list = new ArrayList<>();
        HumorDTO entry;
        try {

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, month);
            stmt.setInt(2, year);
            rs = stmt.executeQuery();
            while (rs.next()) {
                entry = new HumorDTO();
                entry.setDate(convertDate(rs.getDate("day_date")));
                entry.setMood(rs.getString("mood"));
                entry.setNote(rs.getString("note"));
                list.add(entry);
            }

        } catch (SQLException | ParseException e) {
            System.out.println("Erro "+ e.getClass().getSimpleName() +" em selectByCurrentMonth(): " + e.getMessage());
        }
        return list;
    }


    // -------------------- READ --------------------
    // SELECT by day
    public HumorDTO selectByDay(String date) {
        System.out.println("método selectByDate() chamado!");

        String sql = "SELECT * FROM humor_dia WHERE day_date=?";
        conn = new Conexao().conectaBD();
        HumorDTO entry = null;
        try {

            stmt = conn.prepareStatement(sql);
            stmt.setDate(1, convertDate(date));
            rs = stmt.executeQuery();
            if (rs.next()) {
                entry = new HumorDTO();
                entry.setDate(convertDate(rs.getDate("day_date")));
                entry.setMood(rs.getString("mood"));
                entry.setNote(rs.getString("note"));
            }

        } catch (SQLException | ParseException e) {
            System.out.println("Erro "+ e.getClass().getSimpleName() +" em selectByDate(): " + e.getMessage());
        }
        return entry;
    }

    // SELECT by week
    public List<HumorDTO> selectByWeek(String date) {
        System.out.println("método selectByWeek() chamado!");

        String sql = "SELECT * " +
                "FROM humor_dia " +
                "WHERE WEEK(day_date) = WEEK(?) " +
                "AND YEAR(day_date) = YEAR(?)";
        conn = new Conexao().conectaBD();
        List<HumorDTO> list = new ArrayList<>();
        HumorDTO entry;
        try {

            stmt = conn.prepareStatement(sql);
            stmt.setDate(1, convertDate(date));
            stmt.setDate(2, convertDate(date));
            rs = stmt.executeQuery();
            while (rs.next()) {
                entry = new HumorDTO();
                entry.setDate(convertDate(rs.getDate("day_date")));
                entry.setMood(rs.getString("mood"));
                entry.setNote(rs.getString("note"));
                list.add(entry);
            }

        } catch (SQLException | ParseException e) {
            System.out.println("Erro "+ e.getClass().getSimpleName() +" em selectByWeek(): " + e.getMessage());
        }
        return list;
    }

    // SELECT 2 weeks
    public List<HumorDTO> select2Weeks(String date) {
        System.out.println("método select2Weeks() chamado!");

        String sql = "SELECT * " +
                "FROM humor_dia " +
                "WHERE (WEEK(day_date) = WEEK(?) AND YEAR(day_date) = YEAR(?)) " +
                "OR (WEEK(day_date) = WEEK(?)-1 AND YEAR(day_date) = YEAR(?))";
        conn = new Conexao().conectaBD();
        List<HumorDTO> list = new ArrayList<>();
        HumorDTO entry;
        try {

            stmt = conn.prepareStatement(sql);
            stmt.setDate(1, convertDate(date));
            stmt.setDate(2, convertDate(date));
            stmt.setDate(3, convertDate(date));
            stmt.setDate(4, convertDate(date));
            rs = stmt.executeQuery();
            while (rs.next()) {
                entry = new HumorDTO();
                entry.setDate(convertDate(rs.getDate("day_date")));
                entry.setMood(rs.getString("mood"));
                entry.setNote(rs.getString("note"));
                list.add(entry);
            }

        } catch (SQLException | ParseException e) {
            System.out.println("Erro "+ e.getClass().getSimpleName() +" em select2Weeks(): " + e.getMessage());
        }
        return list;
    }

    // SELECT by month
    public List<HumorDTO> selectByMonth(String date) {
        System.out.println("método selectByMonth() chamado!");

        String sql = "SELECT * " +
                "FROM humor_dia " +
                "WHERE MONTH(day_date) = MONTH(?)" +
                "AND YEAR(day_date) = YEAR(?)";
        conn = new Conexao().conectaBD();
        List<HumorDTO> list = new ArrayList<>();
        HumorDTO entry;
        try {

            stmt = conn.prepareStatement(sql);
            stmt.setDate(1, convertDate(date));
            stmt.setDate(2, convertDate(date));
            rs = stmt.executeQuery();
            while (rs.next()) {
                entry = new HumorDTO();
                entry.setDate(convertDate(rs.getDate("day_date")));
                entry.setMood(rs.getString("mood"));
                entry.setNote(rs.getString("note"));
                list.add(entry);
            }

        } catch (SQLException | ParseException e) {
            System.out.println("Erro "+ e.getClass().getSimpleName() +" em selectByMonth(): " + e.getMessage());
        }
        return list;
    }


    // -------------------- UTIL --------------------
    // Converte data para o formato do Backend
    private Date convertDate(String date) throws ParseException {
        return Date.valueOf(
                backDate.format(
                        frontDate.parse(date)
                )
        );
    }
    // Converte data para o formato do Frontend
    private String convertDate(Date date) throws ParseException {
        return frontDate.format(
                backDate.parse(
                        String.valueOf(date)
                )
        );
    }

    // Avança (ou retrocede) dias de um "Date"
    private Date addDays(Date date, int days) {
        // Se days for negativo retrocede dias
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return (Date) cal.getTime();
    }

    // Encontra número de dias do mês de um "Date"
    private int getDaysInMonth(Date date) {
        LocalDate localDate = date.toLocalDate();

        int year = localDate.getYear();
        int month = localDate.getMonthValue();

        YearMonth yearMonth = YearMonth.of(year, month);

        return yearMonth.lengthOfMonth();
    }
}
