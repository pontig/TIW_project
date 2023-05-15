package it.polimi.tiw.dao;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import it.polimi.tiw.beans.Category;
import it.polimi.tiw.beans.Image;
import it.polimi.tiw.exceptions.TooManyChildrenException;

public class CategoryDAO {
	private Connection connection;

	public CategoryDAO(Connection con) {
		this.connection = con;
	}

	public List<Category> getAll(Integer subToEnlight) throws SQLException {
		List<Category> result = new ArrayList<>();
		try {
			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Category WHERE parent_ID IS NULL");
			ResultSet rs = stmt.executeQuery();
			int i = 0; // The number shown on the marker in the list
			while (rs.next()) {
				i++;
				Category node = new Category();
				node.setId(rs.getInt("category_ID"));
				node.setName(rs.getString("name"));
				node.setParentId(rs.getInt("parent_ID")); // TODO: keep an eye on this
				node.setHierarchy(Integer.toString(i));
				boolean isNull = subToEnlight == null;
				if (isNull || subToEnlight != node.getId())
					fillChildren(node, subToEnlight, false, Integer.toString(i));
				else {
					node.enlight();
					fillChildren(node, subToEnlight, true, Integer.toString(i));
				}
				result.add(node);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	private void fillChildren(Category father, Integer subToEnlight, boolean areWeEnlighting, String marker)
			throws SQLException {
		int id = father.getId();
		try {
			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Category WHERE parent_ID = ?");
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			int i = 0;
			while (rs.next()) {
				i++;
				String newMarker = marker + Integer.toString(i);
				Category node = new Category();
				node.setId(rs.getInt("category_ID"));
				node.setName(rs.getString("name"));
				node.setParentId(rs.getInt("parent_ID"));
				node.setHierarchy(newMarker);
				if (areWeEnlighting)
					node.enlight();
				boolean isNull = subToEnlight == null;
				if (isNull || subToEnlight != node.getId())
					fillChildren(node, subToEnlight, areWeEnlighting, newMarker);
				else {
					node.enlight();
					fillChildren(node, subToEnlight, true, newMarker);
				}
				father.getChildren().add(node);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Category getByParentId(int parentId) {
		Category result = new Category();
		try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Category WHERE category_ID = ?")) {
			stmt.setInt(1, parentId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Category node = new Category();
				node.setId(rs.getInt("category_ID"));
				node.setName(rs.getString("name"));
				node.setParentId(rs.getInt("parent_ID"));
				fillChildren(node, null, false, "0");
				result = node;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public ResultSet insertNewCategory(String name, int parent) throws TooManyChildrenException, SQLException {
		String queryCheck = "SELECT COUNT(*) as numChildren FROM Category WHERE parent_ID = ?";
		String queryInsert = "INSERT INTO Category VALUES (NULL, ?, ?)";
		// connection.setAutoCommit(false);
		PreparedStatement pstatement = null; // The statement that checks if numChild < 9
		PreparedStatement insertPreparedStatement = null;
		ResultSet res = null;
		ResultSet insertedID;
		boolean isRadix = parent == -1;
		try {
			if (isRadix)
				queryCheck = "SELECT COUNT(*) as numChildren FROM Category WHERE parent_ID is null";
			pstatement = connection.prepareStatement(queryCheck);
			if (!isRadix)
				pstatement.setInt(1, parent);
			res = pstatement.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		} finally {
			if (res.next() && res.getInt("numChildren") < 9) {
				try {
					if (isRadix)
						queryInsert = "INSERT INTO Category VALUES (NULL, ?, NULL)";
					insertPreparedStatement = connection.prepareStatement(queryInsert, Statement.RETURN_GENERATED_KEYS);
					insertPreparedStatement.setString(1, name);
					if (!isRadix)
						insertPreparedStatement.setInt(2, parent);
					insertPreparedStatement.executeUpdate();
					insertedID = insertPreparedStatement.getGeneratedKeys();
				} catch (SQLException e) {
					e.printStackTrace();
					throw new SQLException(e);

				} finally {
					try {
						pstatement.close();
					} catch (Exception e2) {
						throw new SQLException(e2);
					}
				}
			} else {
				throw new TooManyChildrenException("The number of children exceeds 9");
			}
		}
		return insertedID;
	}

	public List<Image> getImages(int category_ID) throws SQLException {
		List<Image> images = new ArrayList<>();
		String query = "SELECT * FROM image NATURAL JOIN belongs WHERE category_ID = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;

		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, category_ID);
			result = pstatement.executeQuery();
			while (result.next()) {
				Image i = new Image();
				Blob blob = result.getBlob("image");
				byte[] imgData = blob.getBytes(1, (int) blob.length());
				String encodedImg = Base64.getEncoder().encodeToString(imgData);
				i.setImg(encodedImg);
				images.add(i);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		} finally {
			try {
				result.close();
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
			try {
				pstatement.close();
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}
		return images;
	}

	public void uploadImage(int category_id, InputStream image) throws SQLException {
		String queryImg = "INSERT INTO image VALUES (null, ?)";
		String queryBel = "INSERT INTO belongs VALUES (null, ?, ?)";
		PreparedStatement pstatement = null, p2 = null;

		try {
			pstatement = connection.prepareStatement(queryImg, Statement.RETURN_GENERATED_KEYS);
			pstatement.setBlob(1, image);
			pstatement.executeUpdate();
			ResultSet key = pstatement.getGeneratedKeys();
			key.next();
			p2 = connection.prepareStatement(queryBel);
			p2.setInt(1, (int) key.getLong(1));
			p2.setInt(2, category_id);
			p2.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}
		return;
	}
	
	public void rename(int id, String newName) throws SQLException {
		String query = "UPDATE category SET name=? WHERE category_id=?";
		PreparedStatement pstatement = null;
		
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, newName);
			pstatement.setInt(2, id);
			pstatement.executeUpdate();
		} catch (SQLException e) {
			throw new SQLException(e);
		}
		return;
	}

}
