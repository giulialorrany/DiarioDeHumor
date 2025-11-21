package org.example.diariodehumor.dao;

import org.example.diariodehumor.model.Conexao;
import org.example.diariodehumor.model.HumorDTO;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class HumorDAO {

    Connection conn;
    PreparedStatement stmt;
    ResultSet rs;

    static final SimpleDateFormat frontDate = new SimpleDateFormat("E MMM dd yyyy", Locale.ENGLISH);
    static final SimpleDateFormat backDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    // CREATE || UPDATE
    public void save(HumorDTO entry) {
        System.out.println("método save() chamado!");
        if (entry.getDate() != null && selectByDate(entry.getDate()) != null) {
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

    // READ all
    public List<HumorDTO> selectAll() {
        System.out.println("método selectAll() chamado!");
        String sql = "SELECT * FROM humor_dia";
        conn = new Conexao().conectaBD();
        List<HumorDTO> list = new ArrayList<>();
        try {

            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                HumorDTO entry = new HumorDTO();
                entry.setDate(convertDate(rs.getDate("day_date")));
                entry.setMood(rs.getString("mood"));
                entry.setNote(rs.getString("note"));
                list.add(entry);
            }

        } catch (SQLException | ParseException e) {
            System.out.println("Erro "+ e.getClass().getSimpleName() +" em selectAll(): " + e.getMessage());
        }
        return list;
    }

    // READ by date
    public HumorDTO selectByDate(String date) {
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

    // Para o Backend
    private Date convertDate(String date) throws ParseException {

        /*
        date = date.trim().toLowerCase();
        date = Character.toUpperCase(date.charAt(0))
                + date.substring(1, 4)
                + Character.toUpperCase(date.charAt(4))
                + date.substring(5);
         */

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
}
