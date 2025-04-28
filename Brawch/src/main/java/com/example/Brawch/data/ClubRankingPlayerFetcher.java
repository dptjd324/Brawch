package com.example.Brawch.data;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import com.google.gson.*;

public class ClubRankingPlayerFetcher {

    // Bearer 뒤에 자기 API TOKEN 넣어주기 Bearer 지우면 안됨!
    private static final String API_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiIsImtpZCI6IjI4YTMxOGY3LTAwMDAtYTFlYi03ZmExLTJjNzQzM2M2Y2NhNSJ9.eyJpc3MiOiJzdXBlcmNlbGwiLCJhdWQiOiJzdXBlcmNlbGw6Z2FtZWFwaSIsImp0aSI6IjRlZTc1M2UyLTQwM2EtNDY3ZS1hOTA0LWZjMTVkZmMxN2E5MiIsImlhdCI6MTc0NDY1MDUxMiwic3ViIjoiZGV2ZWxvcGVyL2UzYWI0ZGM4LWI3NzctODMyZi04YTRhLTBkMTAzZDQyMWE0MCIsInNjb3BlcyI6WyJicmF3bHN0YXJzIl0sImxpbWl0cyI6W3sidGllciI6ImRldmVsb3Blci9zaWx2ZXIiLCJ0eXBlIjoidGhyb3R0bGluZyJ9LHsiY2lkcnMiOlsiMzUuMTk0LjY1LjkxIl0sInR5cGUiOiJjbGllbnQifV19.GP5Q3wYjJ8uliJ1529GC5pfw7mH4z7aJxFBa_3DFSUon4mzogUUiE1XOnq9b473UeVv25rpOszmhShOyCHfOvw";
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/testdb";
    //db 이름 정해서 aws로 진행할때 이름으로 수정하기
    private static final String DB_USER = "postgres";
    // 유저명
    private static final String DB_PASSWORD = "1111";
    // 설정 비밀번호
    // ⭐⭐ 추가: 실행할 때 작업자가 자신의 batch_id를 지정
    // 조영래 : 1, 이예성 : 2, 강영우 : 3
    private static final int BATCH_ID = 1; // ← 실행자별로 1, 2, 3 지정

    public static void main(String[] args) throws Exception {
        List<String> clubTags = fetchTopClubTags();
        System.out.println("🔍 불러온 클럽 수: " + clubTags.size());

        Set<String> playerTags = new HashSet<>();
        for (String clubTag : clubTags) {
            playerTags.addAll(fetchClubMembers(clubTag));
            Thread.sleep(200); // 요청 간 딜레이 (API 과부하 방지)
        }

        System.out.println("✅ 전체 멤버 수: " + playerTags.size());
        savePlayersToPostgres(playerTags);
    }

    // 1. 글로벌 클럽 랭킹 가져오기
    private static List<String> fetchTopClubTags() throws IOException {
        List<String> tags = new ArrayList<>();
        URL url = new URL("https://api.brawlstars.com/v1/rankings/global/clubs?limit=200");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", API_TOKEN);
        conn.setRequestProperty("Accept", "application/json");

        JsonObject json = JsonParser.parseReader(new InputStreamReader(conn.getInputStream())).getAsJsonObject();
        JsonArray items = json.getAsJsonArray("items");

        for (JsonElement e : items) {
            JsonObject club = e.getAsJsonObject();
            String tag = club.get("tag").getAsString().replace("#", "");
            tags.add(tag);
        }
        return tags;
    }

    // 2. 클럽 멤버 가져오기
    private static List<String> fetchClubMembers(String clubTag) throws IOException {
        List<String> playerTags = new ArrayList<>();
        URL url = new URL("https://api.brawlstars.com/v1/clubs/%23" + clubTag);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", API_TOKEN);
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) return playerTags;

        JsonObject json = JsonParser.parseReader(new InputStreamReader(conn.getInputStream())).getAsJsonObject();
        JsonArray members = json.getAsJsonArray("members");

        for (JsonElement e : members) {
            JsonObject member = e.getAsJsonObject();
            String playerTag = member.get("tag").getAsString().replace("#", "");
            playerTags.add(playerTag);
        }
        return playerTags;
    }

    // 3. 모든 플레이어 저장
    private static void savePlayersToPostgres(Set<String> tags) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "INSERT INTO players (tag, batch_id) VALUES (?, ?) ON CONFLICT (tag) DO NOTHING";
            PreparedStatement stmt = conn.prepareStatement(sql);

            for (String tag : tags) {
                stmt.setString(1, tag);
                stmt.setInt(2, BATCH_ID);
                stmt.addBatch();
            }

            stmt.executeBatch();
            System.out.println("🔒 저장 완료된 플레이어 수: " + tags.size() + " (batch_id = " + BATCH_ID + ")");
        } catch (SQLException e) {
            System.err.println("❌ DB 저장 실패: " + e.getMessage());
        }
    }
}
