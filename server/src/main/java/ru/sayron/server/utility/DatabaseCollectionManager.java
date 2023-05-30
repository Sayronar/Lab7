package ru.sayron.server.utility;

import ru.sayron.common.data.*;
import ru.sayron.common.interaction.OrganizationRaw;
import ru.sayron.common.interaction.User;
import ru.sayron.common.utility.Outputer;
import ru.sayron.server.Main;
import ru.sayron.common.exceptions.DatabaseHandlingException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * Operates the database collection itself.
 */
public class DatabaseCollectionManager {
    // Organization_TABLE
    private final String SELECT_ALL_ORGANIZATIONS = "SELECT * FROM " + DatabaseHandler.ORGANIZATION_TABLE;
    private final String SELECT_ORGANIZATION_BY_ID = SELECT_ALL_ORGANIZATIONS + " WHERE " +
            DatabaseHandler.ORGANIZATION_TABLE_ID_COLUMN + " = ?";
    private final String SELECT_ORGANIZATION_BY_ID_AND_USER_ID = SELECT_ORGANIZATION_BY_ID + " AND " +
            DatabaseHandler.ORGANIZATION_TABLE_USER_ID_COLUMN + " = ?";
    private final String INSERT_ORGANIZATION = "INSERT INTO " +
            DatabaseHandler.ORGANIZATION_TABLE + " (" +
            DatabaseHandler.ORGANIZATION_TABLE_NAME_COLUMN + ", " +
            DatabaseHandler.ORGANIZATION_TABLE_CREATION_DATE_COLUMN + ", " +
            DatabaseHandler.ORGANIZATION_TABLE_ANNUAL_TURNOVER_COLUMN + ", " +
            DatabaseHandler.ORGANIZATION_TABLE_ORGANIZATION_TYPE_COLUMN + ", " +
            DatabaseHandler.ORGANIZATION_TABLE_FULL_NAME_COLUMN + ", " +
            DatabaseHandler.ORGANIZATION_TABLE_EMPLOYEES_COUNT_COLUMN + ", " +
            DatabaseHandler.ORGANIZATION_TABLE_ADDRESS_ID_COLUMN + ", " +
            DatabaseHandler.ORGANIZATION_TABLE_USER_ID_COLUMN + ") VALUES (?, ?, ?, ?," +
            "?::type, ?, ?, ?)";
    private final String DELETE_ORGANIZATION_BY_ID = "DELETE FROM " + DatabaseHandler.ORGANIZATION_TABLE +
            " WHERE " + DatabaseHandler.ORGANIZATION_TABLE_ID_COLUMN + " = ?";
    private final String UPDATE_ORGANIZATION_NAME_BY_ID = "UPDATE " + DatabaseHandler.ORGANIZATION_TABLE + " SET " +
            DatabaseHandler.ORGANIZATION_TABLE_NAME_COLUMN + " = ?" + " WHERE " +
            DatabaseHandler.ORGANIZATION_TABLE_ID_COLUMN + " = ?";
    private final String UPDATE_ORGANIZATION_ANNUAL_TURNOVER_BY_ID = "UPDATE " + DatabaseHandler.ORGANIZATION_TABLE + " SET " +
            DatabaseHandler.ORGANIZATION_TABLE_ANNUAL_TURNOVER_COLUMN + " = ?" + " WHERE " +
            DatabaseHandler.ORGANIZATION_TABLE_ID_COLUMN + " = ?";
    private final String UPDATE_ORGANIZATION_FULL_NAME_BY_ID = "UPDATE " + DatabaseHandler.ORGANIZATION_TABLE + " SET " +
            DatabaseHandler.ORGANIZATION_TABLE_FULL_NAME_COLUMN + " = ?" + " WHERE " +
            DatabaseHandler.ORGANIZATION_TABLE_ID_COLUMN + " = ?";
    private final String UPDATE_ORGANIZATION_EMPLOYEES_COUNT_BY_ID = "UPDATE " + DatabaseHandler.ORGANIZATION_TABLE + " SET " +
            DatabaseHandler.ORGANIZATION_TABLE_EMPLOYEES_COUNT_COLUMN + " = ?" + " WHERE " +
            DatabaseHandler.ORGANIZATION_TABLE_ID_COLUMN + " = ?";
    private final String UPDATE_ORGANIZATION_TYPE_BY_ID = "UPDATE " + DatabaseHandler.ORGANIZATION_TABLE + " SET " +
            DatabaseHandler.ORGANIZATION_TABLE_ORGANIZATION_TYPE_COLUMN + " = ?::type" + " WHERE " +
            DatabaseHandler.ORGANIZATION_TABLE_ID_COLUMN + " = ?";
    // COORDINATES_TABLE
    private final String SELECT_ALL_COORDINATES = "SELECT * FROM " + DatabaseHandler.COORDINATES_TABLE;
    private final String SELECT_COORDINATES_BY_ORGANIZATION_ID = SELECT_ALL_COORDINATES +
            " WHERE " + DatabaseHandler.COORDINATES_TABLE_ORGANIZATION_ID_COLUMN + " = ?";
    private final String INSERT_COORDINATES = "INSERT INTO " +
            DatabaseHandler.COORDINATES_TABLE + " (" +
            DatabaseHandler.COORDINATES_TABLE_ORGANIZATION_ID_COLUMN + ", " +
            DatabaseHandler.COORDINATES_TABLE_X_COLUMN + ", " +
            DatabaseHandler.COORDINATES_TABLE_Y_COLUMN + ") VALUES (?, ?, ?)";
    private final String UPDATE_COORDINATES_BY_ORGANIZATION_ID = "UPDATE " + DatabaseHandler.COORDINATES_TABLE + " SET " +
            DatabaseHandler.COORDINATES_TABLE_X_COLUMN + " = ?, " +
            DatabaseHandler.COORDINATES_TABLE_Y_COLUMN + " = ?" + " WHERE " +
            DatabaseHandler.COORDINATES_TABLE_ORGANIZATION_ID_COLUMN + " = ?";
    // ADDRESS_TABLE
    private final String SELECT_ALL_ADDRESS = "SELECT * FROM " + DatabaseHandler.ADDRESS_TABLE;
    private final String SELECT_ADDRESS_BY_ID = SELECT_ALL_ADDRESS +
            " WHERE " + DatabaseHandler.ADDRESS_TABLE_ID_COLUMN + " = ?";
    private final String INSERT_ADDRESS = "INSERT INTO " +
            DatabaseHandler.ADDRESS_TABLE + " (" +
            DatabaseHandler.ADDRESS_TABLE_STREET_COLUMN + ", " +
            DatabaseHandler.ADDRESS_TABLE_LOCATION_ID_COLUMN + ") VALUES (?, ?)";
    private final String UPDATE_ADDRESS_BY_ID = "UPDATE " + DatabaseHandler.ADDRESS_TABLE + " SET " +
            DatabaseHandler.ADDRESS_TABLE_STREET_COLUMN + " = ?, " +
           // DatabaseHandler.ADD_TABLE_OrganizationS_COUNT_COLUMN + " = ?" + " WHERE " +
            DatabaseHandler.ADDRESS_TABLE_ID_COLUMN + " = ?";
    private final String DELETE_ADDRESS_BY_ID = "DELETE FROM " + DatabaseHandler.ADDRESS_TABLE +
            " WHERE " + DatabaseHandler.ADDRESS_TABLE_ID_COLUMN + " = ?";
    // LOCATION_TABLE
    private final String SELECT_ALL_LOCATION = "SELECT * FROM " + DatabaseHandler.LOCATION_TABLE;
    private final String SELECT_LOCATION_BY_ID = SELECT_ALL_LOCATION +
            " WHERE " + DatabaseHandler.LOCATION_TABLE_ID_COLUMN + " = ?";
    private final String INSERT_LOCATION = "INSERT INTO " +
            DatabaseHandler.LOCATION_TABLE + " (" +
            DatabaseHandler.LOCATION_TABLE_X_COLUMN + ", " +
            DatabaseHandler.LOCATION_TABLE_Y_COLUMN + ", " +
            DatabaseHandler.LOCATION_TABLE_Z_COLUMN + ") VALUES (?, ?, ?, ?)";
    private final String UPDATE_LOCATION_BY_ID = "UPDATE " + DatabaseHandler.LOCATION_TABLE + " SET " +
            DatabaseHandler.LOCATION_TABLE_X_COLUMN + ", " +
            DatabaseHandler.LOCATION_TABLE_Y_COLUMN + ", " +
            DatabaseHandler.LOCATION_TABLE_Z_COLUMN + " = ?" + " WHERE " +
            DatabaseHandler.ADDRESS_TABLE_ID_COLUMN + " = ?";
    private final String DELETE_LOCATION_BY_ID = "DELETE FROM " + DatabaseHandler.LOCATION_TABLE +
            " WHERE " + DatabaseHandler.LOCATION_TABLE_ID_COLUMN + " = ?";


    private DatabaseHandler databaseHandler;
    private DatabaseUserManager databaseUserManager;

    public DatabaseCollectionManager(DatabaseHandler databaseHandler, DatabaseUserManager databaseUserManager) {
        this.databaseHandler = databaseHandler;
        this.databaseUserManager = databaseUserManager;
    }

    /**
     * Create Organization.
     *
     * @param resultSet Result set parametres of Organization.
     * @return New Organization.
     * @throws SQLException When there's exception inside.
     */
    private Organization createOrganization(ResultSet resultSet) throws SQLException {
        long id = resultSet.getLong(DatabaseHandler.ORGANIZATION_TABLE_ID_COLUMN);
        String name = resultSet.getString(DatabaseHandler.ORGANIZATION_TABLE_NAME_COLUMN);
        LocalDateTime creationDate = resultSet.getTimestamp(DatabaseHandler.ORGANIZATION_TABLE_CREATION_DATE_COLUMN).toLocalDateTime();
        int annualTurnover = resultSet.getInt(DatabaseHandler.ORGANIZATION_TABLE_ANNUAL_TURNOVER_COLUMN);
        String fullName = resultSet.getString(DatabaseHandler.ORGANIZATION_TABLE_FULL_NAME_COLUMN);
        Long employeesCount = resultSet.getLong(DatabaseHandler.ORGANIZATION_TABLE_EMPLOYEES_COUNT_COLUMN);
        OrganizationType type = OrganizationType.valueOf(resultSet.getString(DatabaseHandler.ORGANIZATION_TABLE_ORGANIZATION_TYPE_COLUMN));
        Coordinates coordinates = getCoordinatesByOrganizationId(id);
        Address officialAddress = getAddressById(resultSet.getLong(DatabaseHandler.ORGANIZATION_TABLE_ADDRESS_ID_COLUMN));
        User owner = databaseUserManager.getUserById(resultSet.getLong(DatabaseHandler.ORGANIZATION_TABLE_USER_ID_COLUMN));
        return new Organization(
                id,
                name,
                coordinates,
                creationDate,
                annualTurnover,
                fullName,
                employeesCount,
                type,
                officialAddress,
                owner
        );
    }

    /**
     * @return List of Organizations.
     * @throws DatabaseHandlingException When there's exception inside.
     */
    public NavigableSet<Organization> getCollection() throws DatabaseHandlingException {
        NavigableSet<Organization> organizationList = new TreeSet<>();
        PreparedStatement preparedSelectAllStatement = null;
        try {
            preparedSelectAllStatement = databaseHandler.getPreparedStatement(SELECT_ALL_ORGANIZATIONS, false);
            ResultSet resultSet = preparedSelectAllStatement.executeQuery();
            while (resultSet.next()) {
                organizationList.add(createOrganization(resultSet));
            }
        } catch (SQLException exception) {
            throw new DatabaseHandlingException();
        } finally {
            databaseHandler.closePreparedStatement(preparedSelectAllStatement);
        }
        return organizationList;
    }

    /**
     * @param OrganizationId Id of Organization.
     * @return Chapter id.
     * @throws SQLException When there's exception inside.
     */
    private long getAddressIdByOrganizationId(long OrganizationId) throws SQLException {
        long chapterId;
        PreparedStatement preparedSelectOrganizationByIdStatement = null;
        try {
            preparedSelectOrganizationByIdStatement = databaseHandler.getPreparedStatement(SELECT_ORGANIZATION_BY_ID, false);
            preparedSelectOrganizationByIdStatement.setLong(1, OrganizationId);
            ResultSet resultSet = preparedSelectOrganizationByIdStatement.executeQuery();
            Main.logger.info("Выполнен запрос SELECT_Organization_BY_ID.");
            if (resultSet.next()) {
                chapterId = resultSet.getLong(DatabaseHandler.ORGANIZATION_TABLE_ADDRESS_ID_COLUMN);
            } else throw new SQLException();
        } catch (SQLException exception) {
            Main.logger.error("Произошла ошибка при выполнении запроса SELECT_Organization_BY_ID!");
            throw new SQLException(exception);
        } finally {
            databaseHandler.closePreparedStatement(preparedSelectOrganizationByIdStatement);
        }
        return chapterId;
    }

    /**
     * @param OrganizationId Id of Organization.
     * @return coordinates.
     * @throws SQLException When there's exception inside.
     */
    private Coordinates getCoordinatesByOrganizationId(long OrganizationId) throws SQLException {
        Coordinates coordinates;
        PreparedStatement preparedSelectCoordinatesByOrganizationIdStatement = null;
        try {
            preparedSelectCoordinatesByOrganizationIdStatement =
                    databaseHandler.getPreparedStatement(SELECT_COORDINATES_BY_ORGANIZATION_ID, false);
            preparedSelectCoordinatesByOrganizationIdStatement.setLong(1, OrganizationId);
            ResultSet resultSet = preparedSelectCoordinatesByOrganizationIdStatement.executeQuery();
            Main.logger.info("Выполнен запрос SELECT_COORDINATES_BY_Organization_ID.");
            if (resultSet.next()) {
                coordinates = new Coordinates(
                        resultSet.getLong(DatabaseHandler.COORDINATES_TABLE_X_COLUMN),
                        resultSet.getInt(DatabaseHandler.COORDINATES_TABLE_Y_COLUMN)
                );
            } else throw new SQLException();
        } catch (SQLException exception) {
            Main.logger.error("Произошла ошибка при выполнении запроса SELECT_COORDINATES_BY_Organization_ID!");
            throw new SQLException(exception);
        } finally {
            databaseHandler.closePreparedStatement(preparedSelectCoordinatesByOrganizationIdStatement);
        }
        return coordinates;
    }

    /**
     * @param addressId Id of Address.
     * @return Chapter.
     * @throws SQLException When there's exception inside.
     */
    private Address getAddressById(long addressId) throws SQLException {
        Address address;
        PreparedStatement preparedSelectAddressByIdStatement = null;
        try {
            preparedSelectAddressByIdStatement =
                    databaseHandler.getPreparedStatement(SELECT_ADDRESS_BY_ID, false);
            preparedSelectAddressByIdStatement.setLong(1, addressId);
            ResultSet resultSet = preparedSelectAddressByIdStatement.executeQuery();
            Main.logger.info("Выполнен запрос SELECT_CHAPTER_BY_ID.");
            if (resultSet.next()) {
                address = new Address(
                        resultSet.getString(DatabaseHandler.ADDRESS_TABLE_STREET_COLUMN),
                        getLocationById(resultSet.getLong(DatabaseHandler.ADDRESS_TABLE_LOCATION_ID_COLUMN))
                );
            } else throw new SQLException();
        } catch (SQLException exception) {
            Main.logger.error("Произошла ошибка при выполнении запроса SELECT_CHAPTER_BY_ID!");
            throw new SQLException(exception);
        } finally {
            databaseHandler.closePreparedStatement(preparedSelectAddressByIdStatement);
        }
        return address;
    }

    /**
     * @param locationId Id of Location.
     * @return Chapter.
     * @throws SQLException When there's exception inside.
     */
    private Location getLocationById(long locationId) throws SQLException {
        Location town;
        PreparedStatement preparedSelectLocationByIdStatement = null;
        try {
            preparedSelectLocationByIdStatement =
                    databaseHandler.getPreparedStatement(SELECT_ADDRESS_BY_ID, false);
            preparedSelectLocationByIdStatement.setLong(1, locationId);
            ResultSet resultSet = preparedSelectLocationByIdStatement.executeQuery();
            Main.logger.info("Выполнен запрос SELECT_CHAPTER_BY_ID.");
            if (resultSet.next()) {
                town = new Location(
                        resultSet.getInt(DatabaseHandler.LOCATION_TABLE_X_COLUMN),
                        resultSet.getFloat(DatabaseHandler.LOCATION_TABLE_Y_COLUMN),
                        resultSet.getLong(DatabaseHandler.LOCATION_TABLE_Z_COLUMN)
                );
            } else throw new SQLException();
        } catch (SQLException exception) {
            Main.logger.error("Произошла ошибка при выполнении запроса SELECT_CHAPTER_BY_ID!");
            throw new SQLException(exception);
        } finally {
            databaseHandler.closePreparedStatement(preparedSelectLocationByIdStatement);
        }
        return town;
    }

    /**
     * @param OrganizationRaw Organization raw.
     * @param user      User.
     * @return Organization.
     * @throws DatabaseHandlingException When there's exception inside.
     */
    public Organization insertOrganization(OrganizationRaw OrganizationRaw, User user) throws DatabaseHandlingException {
        // TODO: Если делаем орден уникальным, тут че-то много всего менять
        Organization Organization;
        PreparedStatement preparedInsertOrganizationStatement = null;
        PreparedStatement preparedInsertCoordinatesStatement = null;
        PreparedStatement preparedInsertAddressStatement = null;
        PreparedStatement preparedInsertLocationStatement = null;
        try {
            databaseHandler.setCommitMode();
            databaseHandler.setSavepoint();

            LocalDateTime creationTime = LocalDateTime.now();

            preparedInsertOrganizationStatement = databaseHandler.getPreparedStatement(INSERT_ORGANIZATION, true);
            preparedInsertCoordinatesStatement = databaseHandler.getPreparedStatement(INSERT_COORDINATES, true);
            preparedInsertAddressStatement = databaseHandler.getPreparedStatement(INSERT_ADDRESS, true);

            preparedInsertAddressStatement.setString(1, OrganizationRaw.getOfficialAddress().getStreet());
            long locationId;
            if ()
            preparedInsertAddressStatement.setLong(2, LocationId);
            if (preparedInsertAddressStatement.executeUpdate() == 0) throw new SQLException();
            ResultSet generatedChapterKeys = preparedInsertChapterStatement.getGeneratedKeys();
            long chapterId;
            if (generatedChapterKeys.next()) {
                chapterId = generatedChapterKeys.getLong(1);
            } else throw new SQLException();
            Main.logger.info("Выполнен запрос INSERT_CHAPTER.");

            preparedInsertOrganizationStatement.setString(1, OrganizationRaw.getName());
            preparedInsertOrganizationStatement.setTimestamp(2, Timestamp.valueOf(creationTime));
            preparedInsertOrganizationStatement.setDouble(3, OrganizationRaw.getHealth());
            preparedInsertOrganizationStatement.setString(4, OrganizationRaw.getCategory().toString());
            preparedInsertOrganizationStatement.setString(5, OrganizationRaw.getWeaponType().toString());
            preparedInsertOrganizationStatement.setString(6, OrganizationRaw.getMeleeWeapon().toString());
            preparedInsertOrganizationStatement.setLong(7, chapterId);
            preparedInsertOrganizationStatement.setLong(8, databaseUserManager.getUserIdByUsername(user));
            if (preparedInsertOrganizationStatement.executeUpdate() == 0) throw new SQLException();
            ResultSet generatedOrganizationKeys = preparedInsertOrganizationStatement.getGeneratedKeys();
            long OrganizationId;
            if (generatedOrganizationKeys.next()) {
                OrganizationId = generatedOrganizationKeys.getLong(1);
            } else throw new SQLException();
            Main.logger.info("Выполнен запрос INSERT_Organization.");

            preparedInsertCoordinatesStatement.setLong(1, OrganizationId);
            preparedInsertCoordinatesStatement.setDouble(2, OrganizationRaw.getCoordinates().getX());
            preparedInsertCoordinatesStatement.setFloat(3, OrganizationRaw.getCoordinates().getY());
            if (preparedInsertCoordinatesStatement.executeUpdate() == 0) throw new SQLException();
            Main.logger.info("Выполнен запрос INSERT_COORDINATES.");

            Organization = new Organization(
                    spaceOrganizationId,
                    OrganizationRaw.getName(),
                    OrganizationRaw.getCoordinates(),
                    creationTime,
                    OrganizationRaw.getHealth(),
                    OrganizationRaw.getCategory(),
                    OrganizationRaw.getWeaponType(),
                    OrganizationRaw.getMeleeWeapon(),
                    OrganizationRaw.getChapter(),
                    user
            );

            databaseHandler.commit();
            return Organization;
        } catch (SQLException exception) {
            Main.logger.error("Произошла ошибка при выполнении группы запросов на добавление нового объекта!");
            databaseHandler.rollback();
            throw new DatabaseHandlingException();
        } finally {
            databaseHandler.closePreparedStatement(preparedInsertOrganizationStatement);
            databaseHandler.closePreparedStatement(preparedInsertCoordinatesStatement);
            databaseHandler.closePreparedStatement(preparedInsertChapterStatement);
            databaseHandler.setNormalMode();
        }
    }

    /**
     * @param OrganizationRaw Organization raw.
     * @param OrganizationId  Id of Organization.
     * @throws DatabaseHandlingException When there's exception inside.
     */
    public void updateOrganizationById(long OrganizationId, OrganizationRaw OrganizationRaw) throws DatabaseHandlingException {
        // TODO: Если делаем орден уникальным, тут че-то много всего менять
        PreparedStatement preparedUpdateOrganizationNameByIdStatement = null;
        PreparedStatement preparedUpdateOrganizationHealthByIdStatement = null;
        PreparedStatement preparedUpdateOrganizationCategoryByIdStatement = null;
        PreparedStatement preparedUpdateOrganizationWeaponTypeByIdStatement = null;
        PreparedStatement preparedUpdateOrganizationMeleeWeaponByIdStatement = null;
        PreparedStatement preparedUpdateCoordinatesByOrganizationIdStatement = null;
        PreparedStatement preparedUpdateChapterByIdStatement = null;
        try {
            databaseHandler.setCommitMode();
            databaseHandler.setSavepoint();

            preparedUpdateOrganizationNameByIdStatement = databaseHandler.getPreparedStatement(UPDATE_Organization_NAME_BY_ID, false);
            preparedUpdateOrganizationHealthByIdStatement = databaseHandler.getPreparedStatement(UPDATE_Organization_HEALTH_BY_ID, false);
            preparedUpdateOrganizationCategoryByIdStatement = databaseHandler.getPreparedStatement(UPDATE_Organization_CATEGORY_BY_ID, false);
            preparedUpdateOrganizationWeaponTypeByIdStatement = databaseHandler.getPreparedStatement(UPDATE_Organization_WEAPON_TYPE_BY_ID, false);
            preparedUpdateOrganizationMeleeWeaponByIdStatement = databaseHandler.getPreparedStatement(UPDATE_Organization_MELEE_WEAPON_BY_ID, false);
            preparedUpdateCoordinatesByOrganizationIdStatement = databaseHandler.getPreparedStatement(UPDATE_COORDINATES_BY_Organization_ID, false);
            preparedUpdateChapterByIdStatement = databaseHandler.getPreparedStatement(UPDATE_CHAPTER_BY_ID, false);

            if (OrganizationRaw.getName() != null) {
                preparedUpdateOrganizationNameByIdStatement.setString(1, OrganizationRaw.getName());
                preparedUpdateOrganizationNameByIdStatement.setLong(2, OrganizationId);
                if (preparedUpdateOrganizationNameByIdStatement.executeUpdate() == 0) throw new SQLException();
                Main.logger.info("Выполнен запрос UPDATE_Organization_NAME_BY_ID.");
            }
            if (OrganizationRaw.getCoordinates() != null) {
                preparedUpdateCoordinatesByOrganizationIdStatement.setDouble(1, OrganizationRaw.getCoordinates().getX());
                preparedUpdateCoordinatesByOrganizationIdStatement.setFloat(2, OrganizationRaw.getCoordinates().getY());
                preparedUpdateCoordinatesByOrganizationIdStatement.setLong(3, OrganizationId);
                if (preparedUpdateCoordinatesByOrganizationIdStatement.executeUpdate() == 0) throw new SQLException();
                Main.logger.info("Выполнен запрос UPDATE_COORDINATES_BY_Organization_ID.");
            }
            if (OrganizationRaw.getHealth() != -1) {
                preparedUpdateOrganizationHealthByIdStatement.setDouble(1, OrganizationRaw.getHealth());
                preparedUpdateOrganizationHealthByIdStatement.setLong(2, OrganizationId);
                if (preparedUpdateOrganizationHealthByIdStatement.executeUpdate() == 0) throw new SQLException();
                Main.logger.info("Выполнен запрос UPDATE_Organization_HEALTH_BY_ID.");
            }
            if (OrganizationRaw.getCategory() != null) {
                preparedUpdateOrganizationCategoryByIdStatement.setString(1, OrganizationRaw.getCategory().toString());
                preparedUpdateOrganizationCategoryByIdStatement.setLong(2, OrganizationId);
                if (preparedUpdateOrganizationCategoryByIdStatement.executeUpdate() == 0) throw new SQLException();
                Main.logger.info("Выполнен запрос UPDATE_Organization_CATEGORY_BY_ID.");
            }
            if (OrganizationRaw.getWeaponType() != null) {
                preparedUpdateOrganizationWeaponTypeByIdStatement.setString(1, OrganizationRaw.getWeaponType().toString());
                preparedUpdateOrganizationWeaponTypeByIdStatement.setLong(2, OrganizationId);
                if (preparedUpdateOrganizationWeaponTypeByIdStatement.executeUpdate() == 0) throw new SQLException();
                Main.logger.info("Выполнен запрос UPDATE_Organization_WEAPON_TYPE_BY_ID.");
            }
            if (OrganizationRaw.getMeleeWeapon() != null) {
                preparedUpdateOrganizationMeleeWeaponByIdStatement.setString(1, OrganizationRaw.getMeleeWeapon().toString());
                preparedUpdateOrganizationMeleeWeaponByIdStatement.setLong(2, OrganizationId);
                if (preparedUpdateOrganizationMeleeWeaponByIdStatement.executeUpdate() == 0) throw new SQLException();
                Main.logger.info("Выполнен запрос UPDATE_Organization_MELEE_WEAPON_BY_ID.");
            }
            if (OrganizationRaw.getChapter() != null) {
                preparedUpdateChapterByIdStatement.setString(1, OrganizationRaw.getChapter().getName());
                preparedUpdateChapterByIdStatement.setLong(2, OrganizationRaw.getChapter().getOrganizationsCount());
                preparedUpdateChapterByIdStatement.setLong(3, getChapterIdByOrganizationId(OrganizationId));
                if (preparedUpdateChapterByIdStatement.executeUpdate() == 0) throw new SQLException();
                Main.logger.info("Выполнен запрос UPDATE_CHAPTER_BY_ID.");
            }

            databaseHandler.commit();
        } catch (SQLException exception) {
            Main.logger.error("Произошла ошибка при выполнении группы запросов на обновление объекта!");
            databaseHandler.rollback();
            throw new DatabaseHandlingException();
        } finally {
            databaseHandler.closePreparedStatement(preparedUpdateOrganizationNameByIdStatement);
            databaseHandler.closePreparedStatement(preparedUpdateOrganizationHealthByIdStatement);
            databaseHandler.closePreparedStatement(preparedUpdateOrganizationCategoryByIdStatement);
            databaseHandler.closePreparedStatement(preparedUpdateOrganizationWeaponTypeByIdStatement);
            databaseHandler.closePreparedStatement(preparedUpdateOrganizationMeleeWeaponByIdStatement);
            databaseHandler.closePreparedStatement(preparedUpdateCoordinatesByOrganizationIdStatement);
            databaseHandler.closePreparedStatement(preparedUpdateChapterByIdStatement);
            databaseHandler.setNormalMode();
        }
    }

    /**
     * Delete Organization by id.
     *
     * @param OrganizationId Id of Organization.
     * @throws DatabaseHandlingException When there's exception inside.
     */
    public void deleteOrganizationById(long OrganizationId) throws DatabaseHandlingException {
        // TODO: Если делаем орден уникальным, тут че-то много всего менять
        PreparedStatement preparedDeleteChapterByIdStatement = null;
        try {
            preparedDeleteChapterByIdStatement = databaseHandler.getPreparedStatement(DELETE_CHAPTER_BY_ID, false);
            preparedDeleteChapterByIdStatement.setLong(1, getChapterIdByOrganizationId(OrganizationId));
            if (preparedDeleteChapterByIdStatement.executeUpdate() == 0) Outputer.println(3);
            Main.logger.info("Выполнен запрос DELETE_CHAPTER_BY_ID.");
        } catch (SQLException exception) {
            Main.logger.error("Произошла ошибка при выполнении запроса DELETE_CHAPTER_BY_ID!");
            throw new DatabaseHandlingException();
        } finally {
            databaseHandler.closePreparedStatement(preparedDeleteChapterByIdStatement);
        }
    }

    /**
     * Checks Organization user id.
     *
     * @param OrganizationId Id of Organization.
     * @param user Owner of Organization.
     * @throws DatabaseHandlingException When there's exception inside.
     * @return Is everything ok.
     */
    public boolean checkOrganizationUserId(long OrganizationId, User user) throws DatabaseHandlingException {
        PreparedStatement preparedSelectOrganizationByIdAndUserIdStatement = null;
        try {
            preparedSelectOrganizationByIdAndUserIdStatement = databaseHandler.getPreparedStatement(SELECT_Organization_BY_ID_AND_USER_ID, false);
            preparedSelectOrganizationByIdAndUserIdStatement.setLong(1, OrganizationId);
            preparedSelectOrganizationByIdAndUserIdStatement.setLong(2, databaseUserManager.getUserIdByUsername(user));
            ResultSet resultSet = preparedSelectOrganizationByIdAndUserIdStatement.executeQuery();
            Main.logger.info("Выполнен запрос SELECT_Organization_BY_ID_AND_USER_ID.");
            return resultSet.next();
        } catch (SQLException exception) {
            Main.logger.error("Произошла ошибка при выполнении запроса SELECT_Organization_BY_ID_AND_USER_ID!");
            throw new DatabaseHandlingException();
        } finally {
            databaseHandler.closePreparedStatement(preparedSelectOrganizationByIdAndUserIdStatement);
        }
    }

    /**
     * Clear the collection.
     *
     * @throws DatabaseHandlingException When there's exception inside.
     */
    public void clearCollection() throws DatabaseHandlingException {
        NavigableSet<SpaceOrganization> OrganizationList = getCollection();
        for (SpaceOrganization Organization : OrganizationList) {
            deleteOrganizationById(Organization.getId());
        }
    }
}
