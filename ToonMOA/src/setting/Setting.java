package setting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class Setting {
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		File file = new File("txt/webtoon.txt");
		if(!file.exists()) parsing();
		
		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://localhost:3306?useSSL=false&useUnicode=true&characterEncoding=UTF-8";
		String user = "root";
		String password = "1234";
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, user, password);
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery("show databases like 'webtoon'");
			if(rs.next()) {
				stmt.executeUpdate("drop database webtoon");
			}
			
			stmt.executeUpdate("create database webtoon");
			stmt.executeUpdate("use webtoon");
			
			StringBuffer createTable = new StringBuffer();
			createTable.append("create table list ");
			createTable.append("(id int not null primary key auto_increment, ");
			createTable.append("site varchar(10) not null, ");			// 웹툰 사이트 종류
			createTable.append("title varchar(30) not null, ");			// 웹툰 제목
			createTable.append("author varchar(30) not null, ");		// 웹툰 작가
			createTable.append("image varchar(200) not null, ");		// 웹툰 이미지 주소
			createTable.append("link varchar(100) not null, ");			// 웹툰 링크
			createTable.append("newest_title varchar(30) not null, ");	// 웹툰 최신화 제목
			createTable.append("newest_link varchar(100) not null)");	// 웹툰 최신화 링크
			
			stmt.executeUpdate(createTable.toString());
			
			stmt.executeUpdate("alter table list convert to charset utf8");
			
			System.out.println("테이블 생성 완료");
			
			StringBuffer insertData = new StringBuffer();
			insertData.append("insert into list values(?, ?, ?, ?, ?, ?, ?, ?) ");
			PreparedStatement pstmt = conn.prepareStatement(insertData.toString());
			
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
				
				String line = "";
				int flag = 0;
				
				while((line = br.readLine()) != null) {
					if(flag == 0) {
						flag++;
						continue;
					}
					
					String strArray[] = line.split("\t");
					
					for(int j=0; j<strArray.length; j++){
						System.out.print(strArray[j] + "\t");
					}
					System.out.println();
					
					pstmt.setInt(1, Integer.parseInt(strArray[0]));
					pstmt.setString(2, strArray[1]);
					pstmt.setString(3, strArray[2]);
					pstmt.setString(4, strArray[3]);
					pstmt.setString(5, strArray[4]);
					pstmt.setString(6, strArray[5]);
					pstmt.setString(7, strArray[6]);
					pstmt.setString(8, strArray[7]);
					
					pstmt.executeUpdate();
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			stmt.executeQuery("use mysql");
			
			rs = stmt.executeQuery("select host, user, authentication_string from user where user = 'webtoonUser'");
			while(rs.next()) {
				stmt.executeUpdate("drop user 'webtoonUser'@'localhost'");
			}
			stmt.executeUpdate("create user 'webtoonUser'@'localhost' identified by '1234'");
			stmt.executeUpdate("grant select on webtoon.* to 'webtoonUser'@'localhost'");
			
			System.out.println("권한 생성 완료");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				conn.close();
				if(stmt != null) stmt.close();
				if(rs != null) rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void parsing() {
		String naverUrl = "https://comic.naver.com/webtoon/weekday.nhn"; //파싱할 홈페이지의 URL주소
		//String daumUrl = "http://webtoon.daum.net/";
		String foxtoonUrl = "https://www.foxtoon.com/comic";
		String ktoonUrl = "https://www.myktoon.com/web/webtoon/works_list.kt";
	    String naverWebtoon = "https://comic.naver.com";
	    //String daumWebtoon = "http://webtoon.daum.net";
	    String foxtoonWebtoon = "https://www.foxtoon.com";
	    String ktoonWebtoon = "https://v2.myktoon.com/web/works/viewer.kt?timesseq=";
	    
	    /*String daumDay[] = {"#day=mon&tab=day", "#day=tue&tab=day", "#day=wed&tab=day", "#day=thu&tab=day",
	    		"#day=fri&tab=day", "#day=sat&tab=day", "#day=sun&tab=day"};*/

	    ArrayList<String> naverHref = new ArrayList<>();
	    ArrayList<String> naverTitle = new ArrayList<>();
	    ArrayList<String> naverImage = new ArrayList<>();
	    ArrayList<String> naverAuthor = new ArrayList<>();
	    ArrayList<String> naverNumber = new ArrayList<>();	// 최신화 제목
	    ArrayList<String> naverLink = new ArrayList<>();	// 최신화 링크
	    
	    /*ArrayList<String> daumHref = new ArrayList<>();
	    ArrayList<String> daumTitle = new ArrayList<>();
	    ArrayList<String> daumImage = new ArrayList<>();
	    ArrayList<String> daumAuthor = new ArrayList<>();
	    ArrayList<String> daumNumber = new ArrayList<>();
	    ArrayList<String> daumLink = new ArrayList<>();*/
	    
	    ArrayList<String> foxtoonHref = new ArrayList<>();
	    ArrayList<String> foxtoonTitle = new ArrayList<>();
	    ArrayList<String> foxtoonImage = new ArrayList<>();
	    ArrayList<String> foxtoonAuthor = new ArrayList<>();
	    ArrayList<String> foxtoonNumber = new ArrayList<>();
	    ArrayList<String> foxtoonLink = new ArrayList<>();
	    
	    ArrayList<String> ktoonHref = new ArrayList<>();
	    ArrayList<String> ktoonTitle = new ArrayList<>();
	    ArrayList<String> ktoonImage = new ArrayList<>();
	    ArrayList<String> ktoonAuthor = new ArrayList<>();
	    ArrayList<String> ktoonNumber = new ArrayList<>();
	    ArrayList<String> ktoonLink = new ArrayList<>();
	    
	    System.setProperty("webdriver.chrome.driver", "C:/Users/soldesk/git/Toon/ToonMOA/chromedriver/chromedriver.exe");
	    
	    try {
			Document doc = Jsoup.connect(naverUrl).get();
			
			Elements titles = doc.select("div.col_inner ul li div a");

            for(Element e : titles){
                if(!naverTitle.contains(e.getElementsByTag("img").attr("title"))){
                	naverHref.add(naverWebtoon + e.attr("href").trim());
                	naverTitle.add(e.getElementsByTag("img").attr("title").trim());
                	naverImage.add(e.getElementsByTag("img").attr("src").trim());

                    Document document = Jsoup.connect(naverWebtoon + e.attr("href").trim()).get();
                    naverAuthor.add(document.select("span.wrt_nm").text().trim());
                    naverNumber.add(document.select("td.title a").first().text().trim());
                    naverLink.add(naverWebtoon + document.select("td.title a").first().attr("href").trim());
                }
            }
			
			/*for(int i=0; i<daumDay.length; i++) {
				driver.get(daumUrl + daumDay[i]);
				doc = Jsoup.parse(driver.getPageSource());
				//doc = Jsoup.connect(daumUrl + daumDay[i]).get();
				
				titles = doc.select("ul.list_wt li a.link_wt");

				for(Element e : titles){
					if(!daumTitle.contains(e.getElementsByClass("tit_wt").text().trim())) {
						daumHref.add(e.attr("href").trim());
						daumTitle.add(e.getElementsByClass("tit_wt").text().trim());
						daumImage.add(e.getElementsByTag("img").attr("src").trim());
						daumAuthor.add(e.parent().getElementsByClass("txt_info").text().trim());
						
						// 아래는 ajax 인듯... 안된다....
						driver.get(daumWebtoon + e.attr("href").trim());
						Document document = Jsoup.parse(driver.getPageSource());
						System.out.println(document.select("ul.clear_g"));
						daumNumber.add(document.select("li.item_preview a.link_wt strong.tit_wt").first().text().trim());
						daumLink.add(document.select("li.item_preview a.link_wt").attr("href").trim());
						
						System.out.println(document.select("li.item_preview a.link_wt strong.tit_wt").first().text().trim());
						System.out.println(document.select("li.item_preview a.link_wt").attr("href").trim());
						break;
					}
				}
				break;
			}*/
            
            doc = Jsoup.connect(foxtoonUrl).get();
            
            titles = doc.select("ul.comic_schedule_day li.comic a");
            for(Element e : titles) {
            	if(!foxtoonTitle.contains(e.getElementsByClass("title").text().trim())) {
            		foxtoonHref.add(foxtoonWebtoon + e.attr("href").trim());
            		foxtoonTitle.add(e.getElementsByClass("title").text().trim());
            		foxtoonImage.add(e.getElementsByClass("total").attr("src").trim());
            		foxtoonAuthor.add(e.getElementsByClass("author").text().trim());
            		
            		Document document = Jsoup.connect(foxtoonWebtoon + e.attr("href").trim()).get();
            		
            		String title = "";
            		String href = "";
            		
            		for(Element e1 : document.select("ul.comic_item_list li.list a")) {
						if(e1.getElementsByClass("coin").text().trim().equals("무료")) {
							title = e1.getElementsByClass("title").text().trim();
							href = e1.attr("href").trim();
						}
					}
            		
            		foxtoonNumber.add(title);
            		foxtoonLink.add(foxtoonWebtoon + href);
            	}
            }
			
			doc = Jsoup.connect(ktoonUrl).get();
			
			titles = doc.select("li.tm7 a");
			
			WebDriver driver = new ChromeDriver();
			
			for(Element e : titles) {
				if(!ktoonTitle.contains(e.getElementsByTag("strong").text().trim())) {
					ktoonHref.add(e.attr("href").trim().replaceAll("javascript:fncAdultCert\\('", "").replaceAll("'\\)", ""));
					ktoonTitle.add(e.getElementsByTag("strong").text().trim());
					ktoonImage.add(e.getElementsByTag("img").attr("src").trim());
					
					try {
						driver.get(e.attr("href").trim());
						Document document = Jsoup.parse(driver.getPageSource()); 
						ktoonAuthor.add(document.select("a.authorInfoBtn").text().trim());
						
						for(Element e1 : document.select("ul.toon_lst li a.item")) {
							if(e1.attr("data-free").equals("Y")) {
								ktoonNumber.add(e1.select("div.info h4 span").text().trim());
								ktoonLink.add(ktoonWebtoon + e1.parent().attr("id").trim());
							}
						}			
					}
					catch(Exception e1) {
						ktoonAuthor.add("");
						ktoonNumber.add("");
						ktoonLink.add("");
					}
				}
			}
			driver.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    try {
	    	int num = 1;
	    	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("txt/webtoon.txt"), "UTF-8"));
			bw.write("번호\t사이트 종류\t제목\t작가\t이미지\t링크\t최신화 제목\t최신화 링크");
			
			for(int i=0; i<naverHref.size(); i++) {
		    	bw.write("\r\n" + (num++) + "\t" + "네이버" + "\t" + naverTitle.get(i) + "\t" + naverAuthor.get(i) + "\t" + naverImage.get(i) + "\t" + naverHref.get(i) + "\t" + naverNumber.get(i) + "\t" + naverLink.get(i));
		    }
			
			for(int i=0; i<foxtoonHref.size(); i++) {
		    	bw.write("\r\n" + (num++) + "\t" + "폭스툰" + "\t" + foxtoonTitle.get(i) + "\t" + foxtoonAuthor.get(i) + "\t" + foxtoonImage.get(i) + "\t" + foxtoonHref.get(i) + "\t" + foxtoonNumber.get(i) + "\t" + foxtoonLink.get(i));
		    }
			
			for(int i=0; i<ktoonHref.size(); i++) {
		    	bw.write("\r\n" + (num++) + "\t" + "케이툰" + "\t" + ktoonTitle.get(i) + "\t" + ktoonAuthor.get(i) + "\t" + ktoonImage.get(i) + "\t" + ktoonHref.get(i) + "\t" + ktoonNumber.get(i) + "\t" + ktoonLink.get(i));
		    }
			
			bw.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
