package org.example.diariodehumor.dao;

import org.example.diariodehumor.model.Conexao;
import org.example.diariodehumor.model.HumorDTO;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

@Component
public class HumorDAO {

    Connection conn;
    PreparedStatement stmt;
    ResultSet rs;

    static final SimpleDateFormat frontDate = new SimpleDateFormat("E MMM dd yyyy", Locale.ENGLISH);
    static final SimpleDateFormat backDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    // -------------------- CREATE || UPDATE --------------------
    // CREATE || UPDATE
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


    // -------------------- READ --------------------
    // READ analysis
    public List<HumorDTO> analysis(String period, String date) {
        return switch (period) {
            case "week" -> selectByWeek(date);
            case "month" -> selectByMonth(date);
            default -> select2Weeks(date);
        };
    }

    // READ streak
    public int countConsecutiveDays(String todayDate) {
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

    // READ by day
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

    // READ by current month
    public List<HumorDTO> selectByCurrentMonth(int month, int year) {
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

    // READ by week
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

    // READ 2 weeks
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

    // READ by month
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
    // Para o Backend
    private Date convertDate(String date) throws ParseException {
        return Date.valueOf(
                backDate.format(
                        frontDate.parse(date)
                )
        );
    }
    // Para o Frontend
    private String convertDate(Date date) throws ParseException {
        return frontDate.format(
                backDate.parse(
                        String.valueOf(date)
                )
        );
    }

    private Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return (Date) cal.getTime();
    }
}
