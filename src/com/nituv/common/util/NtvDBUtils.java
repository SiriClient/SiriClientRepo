package com.nituv.common.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import com.nituv.common.exceptions.LbiInternalException;
import com.nituv.lbi.inf.Config;
import com.nituv.lbi.inf.Lg;

public class NtvDBUtils {
	static {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (Exception ex) {
			Lg.lgr.error("couldn't load postgres driver.",ex);
		}
	}
	
	public static Connection getConnection() throws LbiInternalException {
		
//		String url = "jdbc:postgresql://localhost:5434/nituv";
//		props.setProperty("user","postgres");
//		props.setProperty("password","servingwithemuna");
		String url = Config.getConfig().getString("url");
		Properties props = new Properties();
		props.setProperty("user",Config.getConfig().getString("username"));
		props.setProperty("password",Config.getConfig().getString("password"));
		props.setProperty("ssl","false");
		try {
			Connection conn = DriverManager.getConnection(url, props);
			return conn;
		} catch (SQLException ex) {
			Lg.lgr.error(ex);
			throw new LbiInternalException(ex);
		}
	}
	
	public static ResultSet executeQuery(
			String query
	) throws LbiInternalException {
		
		Connection con = null;
		try {
			con = getConnection();
			try {
				Statement statement = con.createStatement();
				return statement.executeQuery(query);
				
			} catch (SQLException ex) {
				Lg.lgr.error(query,ex);
				throw new LbiInternalException(ex);
			}
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ex) {
					Lg.lgr.error(ex);
					throw new LbiInternalException(ex);
				}
			}
		}
	}

	
	public static List<Integer> getIntegerColoumn(
			String tableName,
			String columnName,
			String where
	) throws LbiInternalException {
		
		ResultSet rs = executeQuery("SELECT " + columnName + " FROM " + tableName + " WHERE " + where);
		if (rs == null) {
			LbiInternalException ex = new LbiInternalException("rs == null");
			Lg.lgr.error(ex);
			throw ex;
		}
		

		try {
			ArrayList<Integer> list = new ArrayList<Integer>();
			if (rs.isAfterLast() && rs.isBeforeFirst()) {
				return list;
			}
			int newVal;
			while (rs.next()) {
				newVal = rs.getInt(columnName);
			    if (!rs.wasNull()) {
			    	list.add(newVal);
			    } else {
			    	list.add(null);
			    }
			}
			return list;
		} catch (SQLException ex) {
			LbiInternalException e = new LbiInternalException(ex);
			Lg.lgr.error(e);
			throw e;
		}
			// Create a ResultSetHandler implementation to convert the
			// first row into an Object[].
			/*ResultSetHandler<Object[]> h = new ResultSetHandler<Object[]>() {
			public Object[] handle(ResultSet rs) throws SQLException {
			    if (!rs.next()) {
			        return null;
			    }
			
			    ResultSetMetaData meta = rs.getMetaData();
			    int cols = meta.getColumnCount();
			    Object[] result = new Object[cols];
			
			    for (int i = 0; i < cols; i++) {
			        result[i] = rs.getObject(i + 1);
			    }
			
			    return result;
			}
			};
			
			// Create a QueryRunner that will use connections from
			// the given DataSource
			QueryRunner run = new QueryRunner(dataSource);
			
			// Execute the query and get the results back from the handler
			Object[] result = run.query(
			"SELECT * FROM Person WHERE name=?", h, "John Doe");*/
	}
	
	/*public static void getConn2()
	{
		// Create a ResultSetHandler implementation to convert the
		// first row into an Object[].
		ResultSetHandler<Object[]> h = new ResultSetHandler<Object[]>() {
		public Object[] handle(ResultSet rs) throws SQLException {
		    if (!rs.next()) {
		        return null;
		    }
		
		    ResultSetMetaData meta = rs.getMetaData();
		    int cols = meta.getColumnCount();
		    Object[] result = new Object[cols];
		
		    for (int i = 0; i < cols; i++) {
		        result[i] = rs.getObject(i + 1);
		    }
		
		    return result;
		}
		};
		
		// Create a QueryRunner that will use connections from
		// the given DataSource
		QueryRunner run = new QueryRunner(dataSource);
		
		// Execute the query and get the results back from the handler
		Object[] result = run.query(
		"SELECT * FROM cities WHERE city_name LIKE '%?%'", h, "ъм");
	}*/

	public static void test() {
        // First we set up the BasicDataSource.

	        System.out.println("Setting up data source.");
	        DataSource dataSource = setupDataSource();
	        System.out.println("Done.");
	
	        //
	        // Now, we can use JDBC DataSource as we normally would.
	        //
	        Connection conn = null;
	        Statement stmt = null;
	        ResultSet rset = null;
	
	        try {
	            System.out.println("Creating connection.");
	            conn = dataSource.getConnection();
	            System.out.println("Creating statement.");
	            stmt = conn.createStatement();
	            System.out.println("Executing statement.");
	            rset = stmt.executeQuery("SELECT * FROM cities LIMIT 10");
	            System.out.println("Results:");
	            int numcols = rset.getMetaData().getColumnCount();
	            while(rset.next()) {
	                for(int i=1;i<=numcols;i++) {
	                    System.out.print("\t" + rset.getString(i));
	                }
	                System.out.println("");
	            }
	        } catch(SQLException e) {
	            e.printStackTrace();
	        } finally {
	            try { if (rset != null) rset.close(); } catch(Exception e) { }
	            try { if (stmt != null) stmt.close(); } catch(Exception e) { }
	            try { if (conn != null) conn.close(); } catch(Exception e) { }
	        }
	    }
	
	    private static DataSource setupDataSource() {
	    	
	    	String connectURI = Config.getConfig().getString("url");
	    	String username = Config.getConfig().getString("username");
	    	String password = Config.getConfig().getString("password");
	        BasicDataSource ds = new BasicDataSource();
	        ds.setDriverClassName("org.postgresql.Driver");
	        ds.setUrl(connectURI);
	        ds.setUsername(username);
	        ds.setPassword(password);
	        return ds;
	    }
	
//	    private static void printDataSourceStats(DataSource ds) {
//	        BasicDataSource bds = (BasicDataSource) ds;
//	        System.out.println("NumActive: " + bds.getNumActive());
//	        System.out.println("NumIdle: " + bds.getNumIdle());
//	    }
//	
//	    private static void shutdownDataSource(DataSource ds) throws SQLException {
//	        BasicDataSource bds = (BasicDataSource) ds;
//	        bds.close();
//	    }

		public static String dateToSqlString(Date date) {
			if (date == null) {
				return null;
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			return "'" + sdf.format(date) + "'";
		}

		public static int insertOrUpdate(String sql) throws LbiInternalException
		{
			int effectedCount = 0;
			Connection con = null;
			try {
				con = getConnection();
				try {
					Statement statement = con.createStatement();
					effectedCount = statement.executeUpdate(sql); 
					
				} catch (SQLException ex) {
					throw Lg.lgr.throwing(new LbiInternalException(sql,ex));
				}
			} finally {
					closeConnection(con);
			}
			return effectedCount;
		}

		public static void closeConnection(Connection con) throws LbiInternalException 
		{
			if (con == null) return;
			try {
				con.close();
			} catch (SQLException ex) {
				Lg.lgr.error(ex);
				throw new LbiInternalException(ex);
			}
			
		}

		public static <T> List<T> getObjectsList(Class<T> cls, String query) throws LbiInternalException {
			//TODO change to: http://jodd.org/doc/db/dboomquery.html

			QueryRunner run = new QueryRunner();

			// Use the BeanListHandler implementation to convert all
			// ResultSet rows into a List of Person JavaBeans.
						
			ResultSetHandler<List<T>> h = new BeanListHandler<T>(cls);

			// Execute the SQL statement and return the results in a List of
			// Person objects generated by the BeanListHandler.
			List<T> returnedList;
			Connection con = NtvDBUtils.getConnection();
			try {
				returnedList = run.query(con,query, h);
				return returnedList;
			} catch (SQLException ex) {
				throw Lg.lgr.throwing(new LbiInternalException(ex));
			} finally {
				NtvDBUtils.closeConnection(con);
			}			
		}

		@SuppressWarnings("unchecked")
		public static <T> List<T> getColumn(Class<T> cls, String sql) throws LbiInternalException
		{
			ResultSet rs = executeQuery(sql);
			if (rs == null) {
				LbiInternalException ex = new LbiInternalException("rs == null");
				Lg.lgr.error(ex);
				throw ex;
			}
			
			
			try {
				ArrayList<T> list = new ArrayList<T>();
				if (rs.isAfterLast() && rs.isBeforeFirst()) {
					return list;
				}
				T objectTyped;
				Object object;
				while (rs.next()) {
					object = rs.getObject(1);
					if (cls.isInstance(object)) {
						objectTyped = (T) object;
						if (!rs.wasNull()) {
							list.add(objectTyped);
						} else {
							list.add(null);
						}
					}
				}
				return list;
			} catch (SQLException ex) {
				LbiInternalException e = new LbiInternalException(ex);
				Lg.lgr.error(ex);
				throw e;
			}
		}

		public static int updateField(String table, String column, Object value, String where)
			throws LbiInternalException
		{
			if (table == null || column == null) {
				throw new LbiInternalException("table == null || column == null");
			}
			StringBuffer strBuf = new StringBuffer(100 + table.length() + column.length() + where.length());
			strBuf.append("UPDATE "); strBuf.append(table); 
			strBuf.append(" SET "); strBuf.append(column);
			strBuf.append("="); strBuf.append(fixValueForUpdateOrInsert(value));
			if (where != null) {
				strBuf.append(" WHERE "); strBuf.append(where); 
			}
			return insertOrUpdate(strBuf.toString());
						
		}
		
		public static String fixValueForUpdateOrInsert(Object value) 
		{
			if (value == null) {
				return "null";
			}
			
			String valueStr = null;
			if (value instanceof String) {
				valueStr = addApostrophes((String) value);
			} else if (value instanceof Date) {
				valueStr = dateToSqlString((Date)value);
			} else {
				valueStr = value.toString();
			}
			return valueStr;
		}
		
		public static String addApostrophes(final String value) {
			if (value == null) {
				return null;
			}
			StringBuffer strBuf = new StringBuffer(value.length() + 5);
			addApostrophes(strBuf,value);
			return strBuf.toString();
		}
		
		public static void addApostrophes(StringBuffer strBuf, final String value) {
			if (strBuf == null) 
				return;
			if (value == null) {
				strBuf.append("null");
				return;
			}
			strBuf.append("'");
			strBuf.append(value.replaceAll("/'/g", "''"));
			strBuf.append("'");
		}

		public static int updateFields(String table, String columns, Object[] values, String where) 
				throws LbiInternalException
			{
				if (table == null || columns == null || values == null || values.length == 0) {
					throw new LbiInternalException("table == null || columns == null || values == null || values.length == 0");
				}
			StringBuffer strBuf = new StringBuffer(100 + table.length() + columns.length() + (values.length * 20) + where.length());
			strBuf.append("UPDATE "); strBuf.append(table); 
			strBuf.append(" SET ("); strBuf.append(columns);
			strBuf.append(") = ("); 
			strBuf.append(fixValueForUpdateOrInsert(values[0]));
			
			for(int i=1;i<values.length; i++) {
				strBuf.append(",");
				strBuf.append(fixValueForUpdateOrInsert(values[i]));
			}
			
			strBuf.append(")");
			if (where != null) {
				strBuf.append(" WHERE "); strBuf.append(where); 
			}
			return insertOrUpdate(strBuf.toString());
			
		}
}
