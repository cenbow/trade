package com.amarsoft.app.accounting.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.HashMap;

import com.amarsoft.app.accounting.jndi.pool.OCIConfig;
import com.amarsoft.app.accounting.jndi.pool.SingleConnectionFactory;
import com.amarsoft.app.accounting.sysconfig.SystemConfig;
import com.amarsoft.app.accounting.util.ACCOUNT_CONSTANTS;
import com.amarsoft.app.accounting.util.CycleConnection;
import com.amarsoft.are.ARE;
import com.amarsoft.awe.Configure;
import com.amarsoft.awe.util.Transaction;

public class SerialnoGenerator {
	public static int addNum = 1000;
	
	public static String dstype;
	
	
	public static HashMap<String,int[]> serialnos=new HashMap<String,int[]>();
	
	
	public static String getSerialno(String object) throws Exception{

		String businessDate = SystemConfig.getBusinessDate();
		
		String serialNo=""; 
		String key = object+businessDate.replace("/", "");
		int[] serialno=serialnos.get(key);
		//Ϊ�ջ��߻��պ���߳�����Χ����ȡ��ˮ
		if(serialno==null||serialno[0]>=serialno[2]){
			Connection con = null;
			if(object.startsWith("TI")){
				con = getConnectionWeb();
			}else{
				con = getConnection();
			}
			
			String keyColum = "SERIALNO";
			
			if(object.toUpperCase().equals(ACCOUNT_CONSTANTS.Table_Name_TRANSRLOG)){
				keyColum = "LOGID";
			}
			
			String[] strno = getNo(object,keyColum,"","yyyyMMdd",businessDate,"00000000",addNum,con);
			if(!con.isClosed()) {
				con.close();
				con = null;
			}
			serialno=new int[3];
			serialno[0]=Integer.parseInt(strno[0])+1;//current
			serialno[1]=0;//min
			serialno[2]=Integer.parseInt(strno[1]);//max
			serialnos.put(key, serialno);
		}
		serialno[0]++;
		
		serialNo = businessDate.replace("/", "") + new DecimalFormat("00000000").format(serialno[0]-1);
		
		return serialNo;
	}
	
	private static Connection getConnectionWeb() throws Exception {
		//web�����ȡ���ݿ⣬��amarsoft.xml��ȡ	
		 Connection connection = CycleConnection.getConnection(ACCOUNT_CONSTANTS.DATABASE_WEBLOAN);
		 return connection;
	}

	public static String getSerialno(String businessDate,String object) throws Exception{

		if("ACCT_BUSINESS_OFF_BALANCE".equals(object))	object = "ACCT_LOAN";
		if("ACCT_FEE_DETAIL".equals(object))	object = "ACCT_LOAN";
		
		String serialNo="";
		String key = object+businessDate.replace("/", "");
		int[] serialno=serialnos.get(key);
		//Ϊ�ջ��߻��պ���߳�����Χ����ȡ��ˮ
		if(serialno==null||serialno[0]>=serialno[2]){
			Connection con = null;
			String[] strno  =null;
			try{
				con = getConnection();
				strno = getNo(object,"SerialNo","","yyyyMMdd",businessDate,"00000000",addNum,con);
			}finally{
				if(con!=null && !con.isClosed()) {
					con.close();
					con = null;
				}
			}								
			serialno=new int[3];
			serialno[0]=Integer.parseInt(strno[0])+1;//current
			serialno[1]=0;//min
			serialno[2]=Integer.parseInt(strno[1]);//max
			serialnos.put(key, serialno);
		}
		serialno[0]++;
		
		serialNo = businessDate.replace("/", "") + new DecimalFormat("00000000").format(serialno[0]-1);
		
		return serialNo;
	}
	
	public static Connection getConnection() throws Exception{
		//web�����ȡ���ݿ⣬��amarsoft.xml��ȡ	
		 Connection connection;
//		if("Web".equals(dstype)){//webӦ��
//			 com.amarsoft.config.ASConfigure CurConfig =  com.amarsoft.config.ASConfigure.getASConfigure();
//			 String dsname = (String)CurConfig.getConfigure(OCIConfig.getDataSourceName());
//			 javax.sql.DataSource ds = ConnectionManager.getDataSource(dsname);
//			 connection = ConnectionManager.getConnection(ds);
//		 }
//		//��ѭ�����ȡ���ݿ�����
//		 else 
		if("Task".equals(dstype)){ 
			 connection = SingleConnectionFactory.getInstance().getConnection(ACCOUNT_CONSTANTS.DATABASE_LOAN);
		 }else if("Web".equals(dstype)){
				/*Configure CurConfig = Configure.getInstance();
			 String dsname = (String)CurConfig.getConfigure(OCIConfig.getDataSourceName());*/
			 String dsname = ARE.getProperty(ACCOUNT_CONSTANTS.DATABASE_ALS);
			 Transaction Sqlca_DS = new Transaction(dsname);
			 connection = Sqlca_DS.getConnection();
		 }else{
			 connection = CycleConnection.getConnection(ACCOUNT_CONSTANTS.DATABASE_LOAN);
		 }
		 return connection;
	}
	
	/**	
	
	 * ��ȡ��ˮ��
	 * @param sTableName
	 * @param sColumnName
	 * @param preName  ǰ׺
	 * @param sDateFormat eg:'yyyyMMdd'
	 * @param sDate
	 * @param sNoFormat  ���ں����λ  eg:��000000��
	 * @param addNum  �����С
	 * @param connection
	 * @return ���飺��С���������
	 * @throws Exception
     * @author jxiong
	 */
	public static String[] getNo(String sTableName, String sColumnName, String preName,
			String sDateFormat,String sBusinessDate,String sNoFormat,int addNum,Connection con) throws Exception {
		//ƽ������ɨ�������������޷�ͨ��ɨ��Ĵ���ת�Ƶ�com.pingan�����棨�ð�����ɨ�裩 2013-06-26 btan
		return com.pingann.core.accounting.SerialNoGenerator
				.getNo(sTableName, sColumnName, preName, sDateFormat, sBusinessDate, sNoFormat, addNum, con);
		
//		//�������ݸ�ʽ������
//		DecimalFormat decimalformat = new DecimalFormat(sNoFormat);
//		//���ڸ�ʽת��
//		SimpleDateFormat simpledateformat = new SimpleDateFormat(sDateFormat);
//		Date date = new Date(sBusinessDate);
//		String sDate=  simpledateformat.format(date);
//	    //��������
//		int iDateLen = sDate.length();
//		int iMaxNo = 0; //current serialno
// 		String[] serialNos = new String[2];
// 		String sNewSerialNo = "";
// 		sTableName = sTableName.toUpperCase();
// 		sColumnName = sColumnName.toUpperCase();
// 		ResultSet rs = null;
//        PreparedStatement ps = null;
// 			String sSql = "select MaxSerialNo from OBJECT_MAXSN "
// 					+ " where TableName=? and ColumnName=? and DateFmt = ? and NoFmt = ? ";
// 			try {
// 			ps = con.prepareStatement(sSql); 
// 			System.out.println(sSql);
// 			ps.setString(1, sTableName);
// 			ps.setString(2, sColumnName);
// 			ps.setString(3, preName+sDateFormat);
// 			ps.setString(4, sNoFormat);
// 			rs = ps.executeQuery();
// 			if (rs.next()) {
// 				// �����ˮ�Ŵ��ڣ���������ˮ�š�
// 				String sMaxSerialNo = rs.getString(1);
// 				if(sMaxSerialNo.startsWith(preName))
// 					sMaxSerialNo = sMaxSerialNo.substring(preName.length());  //ȡ��ǰ׺
// 				// �����ˮ�Ŵ�����Ϊͬһ�죬����ˮ�Ŵӵ�ǰ��������У������1��ʼ��
// 				iMaxNo = 0;
// 				if((sMaxSerialNo.length()-iDateLen)>sNoFormat.length())
// 				{
// 					sMaxSerialNo = sDate + sMaxSerialNo.substring(iDateLen+(sMaxSerialNo.length()-iDateLen-sNoFormat.length()));
// 				}
// 				//��ֹ���ڵ���ʱ��ˮ��ȡ��ͻ
// 				if(!sMaxSerialNo.substring(0,sDate.length()).equals(sDate)){
//// 					String sql = getSQL(sColumnName,sTableName,preName+sDate,con);
// 					String sql = "select max("+sColumnName+") from "+sTableName+" where "+sColumnName+" LIKE '"+preName+sDate+"%'";
// 					PreparedStatement ps1 = con.prepareStatement(sql);
// 					ResultSet rset = ps1.executeQuery();
// 					if(rset.next()){
// 						String serialno = DataConvert.toString(rset.getString(1)).replace(preName, "");
// 						if(!"".equals(serialno))
// 							sMaxSerialNo = serialno;
// 					}
// 					if(ps1!=null){
// 						ps1.close();
// 						ps1 = null;
// 					}
//
// 					if(rset!=null){
// 						rset.close();
// 						rset = null;
// 					}
// 				}
// 				
// 				iMaxNo = Integer.valueOf(sMaxSerialNo.substring(iDateLen))
// 							.intValue();
// 				
// 				//һ�λ�ȡaddNum����ˮ
// 				int tmp = iMaxNo+addNum;
// 				sMaxSerialNo = sDate + decimalformat.format(tmp);
// 				sNewSerialNo = preName + sMaxSerialNo;
// 				// ��������ˮ��
// 				sSql = "update OBJECT_MAXSN set MaxSerialNo =?  where TableName=? and ColumnName=?" +
// 						" and MaxSerialNo = ?  and DateFmt = ? and NoFmt = ?  ";
// 				ps =  con.prepareStatement(sSql);
// 				ps.setString(1, sNewSerialNo);
// 				ps.setString(2, sTableName);
// 				ps.setString(3, sColumnName);
// 				ps.setString(4, rs.getString(1));
// 				ps.setString(5, preName+sDateFormat);
// 				ps.setString(6, sNoFormat);
// 				int i = ps.executeUpdate();
// 				//���δ���µ����ݣ��������»�ȡ��ˮ
// 				if(i == 0) 
// 				{
// 					serialNos = getNo(sTableName, sColumnName, preName, sDateFormat, sBusinessDate, sNoFormat, addNum, con);
// 				}
// 			} else {
// 				// �����ˮ�Ų����ڣ���ֱ�Ӵ�ָ�������ݱ��л�ã����������ˮ�ű��в�����Ӧ��¼��
// 				try
// 				{
//	 	 			iMaxNo = 1;
//	 	 			sNewSerialNo = preName + sDate + decimalformat.format(iMaxNo + addNum);
//	 				
//					sSql = "insert into OBJECT_MAXSN (tablename,columnname,maxserialno,datefmt,nofmt) "
//	 						+ " values(?,?,?,?,?)";
//					ps =  con.prepareStatement(sSql);
//					ps.setString(1, sTableName);
//					ps.setString(2, sColumnName);
//					ps.setString(3, sNewSerialNo);
//					ps.setString(4, preName+sDateFormat);
//					ps.setString(5, sNoFormat);
//					ps.executeUpdate();
// 				}catch(SQLException ex)
// 				{
// 					ex.printStackTrace();
// 					serialNos = getNo(sTableName, sColumnName, preName, sDateFormat, sBusinessDate, sNoFormat, addNum, con);
// 				}
// 			}
// 			
// 		} catch (Exception e) {
// 			e.printStackTrace();
// 			throw new Exception("��ȡ��ˮ��ʧ�ܣ�" + e.getMessage());
// 		} finally {
// 			if(rs != null){
// 				rs.close();
// 				rs = null;
// 			}
// 			if(ps != null){
// 				ps.close();
// 				ps =  null;
// 			}
// 			if(con != null){
// 				con.close();
// 				con =  null;
// 			}
// 			
// 			
// 			
// 		}
// 		
// 		serialNos[0] = decimalformat.format(iMaxNo);
// 		serialNos[1] = decimalformat.format(iMaxNo + addNum);
// 		return serialNos;
 	}
	
	public static String getSQL(String sColumnName,String sTableName,String temp,Connection conn) throws Exception{
		String sql = " select getmaxserialno(?,?,?) from dual";
		PreparedStatement ps11 = null;
		ResultSet rs11 = null;
		String sSQL ="";
		 try{
			 ps11 = conn.prepareStatement(sql);
			 ps11.setString(1, sColumnName);
			 ps11.setString(2, sTableName);
			 ps11.setString(3, temp);
			 rs11 = ps11.executeQuery();
			 while(rs11.next()){
				 sSQL = rs11.getString(1);
			 }
		 }finally{
			 if(ps11 != null){
				 ps11.close();
				 ps11 = null;
			 }
			 if(rs11 != null){
				 rs11.close();
				 rs11 = null;
			 }
			
		 }
		if("".equals(sSQL)||null == sSQL){
			throw new Exception("��ֹ���ڵ���ʱ��ˮ��ȡ��ͻ������ȡ��ˮʧ�ܡ�������");
		}
		//System.out.println(sSQL);
		return sSQL;
	}
	
	
}
