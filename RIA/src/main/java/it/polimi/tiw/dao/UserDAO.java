package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.beans.User;

public class UserDAO {
	private Connection connection;

	public UserDAO(Connection conn) {
		this.connection = conn;
	}

	public User checkCredentials(String user, String psw) throws SQLException {
		String query = "SELECT * FROM Account WHERE username = ? AND password =?";
		try (PreparedStatement statement = connection.prepareStatement(query);) {
			statement.setString(1, user);
			statement.setString(2, psw);
			try (ResultSet result = statement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results
					return null;
				else {
					result.next();
					User res = new User();
					res.setUsername(result.getString("username"));
					return res;
				}

			}
		}
	}
}
