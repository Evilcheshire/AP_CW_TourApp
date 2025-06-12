package tourapp.util;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

public class DaoUtils {

    public static void setParameter(PreparedStatement stmt, int index, Object value) throws SQLException {
        switch (value) {
            case null -> stmt.setNull(index, Types.NULL);
            case Integer i -> stmt.setInt(index, i);
            case Long l -> stmt.setLong(index, l);
            case Double d -> stmt.setDouble(index, d);
            case Float f -> stmt.setFloat(index, f);
            case Boolean b -> stmt.setBoolean(index, b);
            case String s -> stmt.setString(index, s);
            case Timestamp ts -> stmt.setTimestamp(index, ts);
            case java.sql.Date date -> stmt.setDate(index, date);
            case Date utilDate -> stmt.setDate(index, new java.sql.Date(utilDate.getTime()));
            default -> stmt.setObject(index, value);
        }
    }

    public static class JoinInfo {
        private final String joinType;
        private final String joinTable;
        private final String alias;
        private final String condition;
        private final Set<String> requiredParams;

        public JoinInfo(String joinType, String joinTable, String alias, String condition) {
            this(joinType, joinTable, alias, condition, new String[0]);
        }

        public JoinInfo(String joinType, String joinTable, String alias, String condition, String... requiredParams) {
            this.joinType = joinType;
            this.joinTable = joinTable;
            this.alias = alias;
            this.condition = condition;
            this.requiredParams = new HashSet<>(Arrays.asList(requiredParams));
        }

        public boolean isRequired(Set<String> activeParams) {
            return requiredParams.isEmpty() || requiredParams.stream().anyMatch(activeParams::contains);
        }

        public String getJoinSql() {
            return joinType + " " + joinTable + " " + alias + " ON " + condition;
        }

        public String getAlias() {
            return alias;
        }

        public String getCondition() {
            return condition;
        }

        public String getJoinTable() {
            return joinTable;
        }
    }

    public static StringBuilder buildWhereClause(String baseTable, String baseAlias,
                                                 Map<String, Object> searchParams,
                                                 Map<String, String> columnMappings,
                                                 List<JoinInfo> joinInfos) {
        Set<String> activeParams = searchParams.keySet();

        Set<String> addedJoins = new HashSet<>();

        Map<String, Set<String>> joinAliasToTables = new HashMap<>();
        Map<String, Set<String>> tableToJoinAliases = new HashMap<>();

        for (JoinInfo joinInfo : joinInfos) {
            String condition = joinInfo.getCondition();
            String[] parts = condition.split(" (AND|OR) | ON | = ");

            for (String part : parts) {
                String trimmedPart = part.trim();
                if (trimmedPart.contains(".")) {
                    String alias = trimmedPart.split("\\.")[0].trim();

                    if (!alias.equals(baseAlias) && !alias.equals(joinInfo.getAlias())) {
                        joinAliasToTables.computeIfAbsent(joinInfo.getAlias(), k -> new HashSet<>())
                                .add(alias);
                        tableToJoinAliases.computeIfAbsent(alias, k -> new HashSet<>())
                                .add(joinInfo.getAlias());
                    }
                }
            }
        }

        Set<String> requiredJoins = new HashSet<>();

        for (Map.Entry<String, Object> entry : searchParams.entrySet()) {
            String param = entry.getKey();
            String column = resolveColumn(param, columnMappings);

            if (column != null && column.contains(".")) {
                String alias = column.split("\\.")[0].trim();
                if (!alias.equals(baseAlias)) {
                    requiredJoins.add(alias);
                }
            }
        }

        Set<String> allRequiredJoins = new HashSet<>(requiredJoins);
        boolean changed;
        do {
            changed = false;
            Set<String> newJoins = new HashSet<>();

            for (String join : allRequiredJoins) {
                if (joinAliasToTables.containsKey(join)) {
                    for (String dependency : joinAliasToTables.get(join)) {
                        if (!allRequiredJoins.contains(dependency)) {
                            newJoins.add(dependency);
                            changed = true;
                        }
                    }
                }
            }

            allRequiredJoins.addAll(newJoins);
        } while (changed);

        List<JoinInfo> filteredJoins = new ArrayList<>();
        for (JoinInfo joinInfo : joinInfos) {
            if (joinInfo.isRequired(activeParams) || allRequiredJoins.contains(joinInfo.getAlias())) {
                filteredJoins.add(joinInfo);
            }
        }

        List<JoinInfo> sortedJoins = new ArrayList<>();
        Set<String> processedJoins = new HashSet<>();

        while (sortedJoins.size() < filteredJoins.size()) {
            boolean addedAny = false;

            for (JoinInfo joinInfo : filteredJoins) {
                if (!processedJoins.contains(joinInfo.getAlias())) {
                    boolean allDependenciesProcessed = true;

                    if (joinAliasToTables.containsKey(joinInfo.getAlias())) {
                        for (String dependency : joinAliasToTables.get(joinInfo.getAlias())) {
                            if (!dependency.equals(baseAlias) && !processedJoins.contains(dependency)) {
                                allDependenciesProcessed = false;
                                break;
                            }
                        }
                    }

                    if (allDependenciesProcessed) {
                        sortedJoins.add(joinInfo);
                        processedJoins.add(joinInfo.getAlias());
                        addedAny = true;
                    }
                }
            }

            if (!addedAny) {
                for (JoinInfo joinInfo : filteredJoins) {
                    if (!processedJoins.contains(joinInfo.getAlias())) {
                        sortedJoins.add(joinInfo);
                        processedJoins.add(joinInfo.getAlias());
                    }
                }
                break;
            }
        }

        StringBuilder selectPart = new StringBuilder();
        selectPart.append(baseAlias).append(".*");

        for (JoinInfo joinInfo : sortedJoins) {
            String joinAlias = joinInfo.getAlias();

            if (joinInfo.getJoinTable().equals("location_types")) {
                selectPart.append(", ").append(joinAlias).append(".id AS location_type_id");
                selectPart.append(", ").append(joinAlias).append(".name AS location_type_name");
            }

            else if (joinInfo.getJoinTable().equals("transport_types")) {
                selectPart.append(", ").append(joinAlias).append(".id AS type_id");
                selectPart.append(", ").append(joinAlias).append(".name AS type_name");
            }
        }

        StringBuilder query = new StringBuilder("SELECT DISTINCT ")
                .append(selectPart)
                .append(" FROM ")
                .append(baseTable)
                .append(" ")
                .append(baseAlias);

        for (JoinInfo joinInfo : sortedJoins) {
            query.append(" ").append(joinInfo.getJoinSql());
            addedJoins.add(joinInfo.getAlias());
        }

        query.append(" WHERE 1=1");

        for (Map.Entry<String, Object> entry : searchParams.entrySet()) {
            String param = entry.getKey();
            Object value = entry.getValue();

            if (param.equals("keyword") && value instanceof String) {
                if (baseTable.equals("locations")) {
                    query.append(" AND (").append(baseAlias).append(".name LIKE ? OR ")
                            .append(baseAlias).append(".description LIKE ? OR ")
                            .append(baseAlias).append(".country LIKE ?)");
                }
                else {
                    query.append(" AND (").append(baseAlias).append(".name LIKE ? OR tt.name LIKE ?)");
                }
                continue;
            }

            String column = resolveColumn(param, columnMappings);
            if (column == null) continue;

            if (value instanceof List<?> list) {
                if (!list.isEmpty()) {
                    query.append(" AND ").append(column).append(" IN (")
                            .append("?,".repeat(list.size()))
                            .deleteCharAt(query.length() - 1)
                            .append(")");
                }
            } else if (param.startsWith("min")) {
                query.append(" AND ").append(column).append(" >= ?");
            } else if (param.startsWith("max")) {
                query.append(" AND ").append(column).append(" <= ?");
            } else if (param.startsWith("start")) {
                query.append(" AND ").append(column).append(" >= ?");
            } else if (param.startsWith("end")) {
                query.append(" AND ").append(column).append(" <= ?");
            } else if (value instanceof String) {
                query.append(" AND ").append(column).append(" LIKE ?");
            } else {
                query.append(" AND ").append(column).append(" = ?");
            }
        }

        return query;
    }

    public static void setWhereParameters(PreparedStatement stmt, Map<String, Object> searchParams,
                                          Map<String, String> columnMappings) throws SQLException {
        int paramIndex = 1;

        for (Map.Entry<String, Object> entry : searchParams.entrySet()) {
            String param = entry.getKey();
            Object value = entry.getValue();

            if (param.equals("keyword") && value instanceof String) {
                String searchValue = "%" + value + "%";

                stmt.setString(paramIndex++, searchValue);
                stmt.setString(paramIndex++, searchValue);
                stmt.setString(paramIndex++, searchValue);
                continue;
            }

            String column = resolveColumn(param, columnMappings);
            if (column == null) continue;

            if (value instanceof List<?> list) {
                for (Object item : list) {
                    setParameter(stmt, paramIndex++, item);
                }
            } else if (value instanceof String && !param.equals("id")) {
                stmt.setString(paramIndex++, "%" + value + "%");
            } else {
                setParameter(stmt, paramIndex++, value);
            }
        }
    }

    public static <T> List<T> executeSearchQuery(Connection connection, String baseTable, String baseAlias,
                                                 Map<String, Object> searchParams,
                                                 Map<String, String> columnMappings,
                                                 List<JoinInfo> joinInfos,
                                                 ResultSetMapper<T> mapper) throws SQLException {

        StringBuilder queryBuilder = buildWhereClause(baseTable, baseAlias, searchParams, columnMappings, joinInfos);

        queryBuilder.append(" ORDER BY ").append(baseAlias).append(".name");

        try (PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString())) {
            setWhereParameters(stmt, searchParams, columnMappings);

            try (ResultSet rs = stmt.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapper.map(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            System.err.println("SQL Error executing query: " + queryBuilder.toString());
            System.err.println("Parameters: " + searchParams.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining(", ")));
            throw e;
        }
    }

    static String resolveColumn(String param, Map<String, String> columnMappings) {
        if (columnMappings.containsKey(param)) return columnMappings.get(param);

        if (param.startsWith("min") || param.startsWith("max") || param.startsWith("start") || param.startsWith("end")) {
            String stripped = param.replaceFirst("^(min|max|start|end)", "");
            if (!stripped.isEmpty()) {
                stripped = Character.toLowerCase(stripped.charAt(0)) + stripped.substring(1);
                return columnMappings.get(stripped);
            }
        }
        return null;
    }

    @FunctionalInterface
    public interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }
}