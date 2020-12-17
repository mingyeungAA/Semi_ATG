package com.atg.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.websocket.Session;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.atg.Register.biz.RegisterBiz;
import com.atg.Register.biz.RegisterBizImpl;
import com.atg.Register.dao.RegisterDao;
import com.atg.Register.dao.RegisterDaoImpl;
import com.atg.Register.dto.RegisterDto;

@WebServlet("/RegisterController")
public class RegisterController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public RegisterController() {
	}


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");


		String command  = request.getParameter("command");

		System.out.println("<"+command+">");
		HttpSession session = request.getSession();

		//비즈 객체 생성
		RegisterBiz biz = new RegisterBizImpl();

		// 회원가입 창 
		if(command.equals("insert")) {
			System.out.println("insert");
			String mb_name = request.getParameter("name");
			String mb_gender = request.getParameter("gender");
			String mb_id = request.getParameter("id");
			String mb_pw = request.getParameter("pw");
			String mb_email = request.getParameter("email");
			String mb_addr = request.getParameter("addr");
			RegisterDto dto = new RegisterDto(0,mb_id,mb_pw,mb_name,mb_gender,mb_email,mb_addr);
			System.out.println("dto----controller----------"+dto);

			int res = biz.insert(dto);
			
			System.out.println(res);
			if(res>0) {
				jsResponse("회원가입 성공", "login.jsp",	 response);
			}else {
				jsResponse("회원가입 실패", "registform.jsp",response);

			}


			//id 중복체크 
		}else if(command.equals("idcheck")) {
			//1.
			String id= request.getParameter("id");
			//2.
			System.out.println(id+"String id");
			int result = biz.idCheck(id);
			System.out.println(result);
			//					System.out.println(dto+"CONtroller부분");
			//					boolean idnotused = true;		// 아이디 사용 안되고 있다 
			//					System.out.println(idnotused);
			//					if(dto.getMb_id()!= null){
			//						idnotused= false;
			//					}
			//3.
			//4.
			response.sendRedirect("idcheck.jsp?idnotused="+result);

			//login
		}else if(command.equals("login")) {
			String mb_id=request.getParameter("id");
			String mb_pw=request.getParameter("password");

			Map<String, String> map = new HashMap<String, String>();
			map.put("mb_id", mb_id);
			map.put("mb_pw", mb_pw);

			System.out.println(map+"------map------");

			RegisterDto dto =  biz.login(map);
			// biz login에 값을 넣어 다시 dto에 저장 
			System.out.println(dto+"dto 값 저장 확인");
			if(dto!=null) {
				session.setAttribute("LDto", dto);
				System.out.println(dto);
				session.setMaxInactiveInterval(1*60);//1분동안 이벤트가 없다면 자동으로 로그아웃(자동으로세션삭제)
				//admin일 경우 관리자 페이지
				//아니면 
				if(dto.getMb_grade().equals("ADMIN")) {
					jsResponse("관리자 로그인 성공", "adminmain.jsp", response);
				}else {
					jsResponse("회원로그인 성공", "main.jsp", response);
				}
			}else {
				jsResponse("아이디와 패스워드를 다시 입력해주세요", "login.jsp", response);
			}

			//로그아웃창 
		}else if (command.equals("logout")) {
			session.invalidate();
			response.sendRedirect("login.jsp");
			//아이디 비밀번호찾기 창 	
		}else if(command.equals("IDPWfind")) {
			response.sendRedirect("IDPWfind.jsp");
			
			
		}else if(command.equals("emailCheck")) {
			response.sendRedirect("EmailCheck.jsp");
		
		
		}else if(command.equals("naver")) {
			
			String access_token = (String) session.getAttribute("access_token");
			String refresh_token = (String)session.getAttribute("refresh_token");

			if (access_token != null) {
				try {
					String apiurl = "https://openapi.naver.com/v1/nid/me";
					URL url = new URL(apiurl);
					String header = "Bearer " + access_token;
					HttpURLConnection con = (HttpURLConnection) url.openConnection();
					con.setRequestMethod("GET");
					con.setRequestProperty("Authorization", header);
					int responseCode = con.getResponseCode();
					BufferedReader br;
					if (responseCode == 200) { // 정상 호출
						br = new BufferedReader(new InputStreamReader(con.getInputStream()));

					} else { // 에러 발생
						br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
					}

					String inputLine;
					StringBuffer res = new StringBuffer();
					while ((inputLine = br.readLine()) != null) {
						res.append(inputLine);
					}
					
					System.out.println(responseCode);
					System.out.println(res);
					br.close();
					
		    	        JSONParser parsing = new JSONParser();
		    	        Object obj = parsing.parse(res.toString());
		    	        JSONObject jsonObj = (JSONObject)obj;
		    	        JSONObject resObj = (JSONObject)jsonObj.get("response");
		    	        
		    	        String naverCode = (String)resObj.get("id");
		    	        String email = (String)resObj.get("email");
		    	        String name = (String)resObj.get("name");
		    	        //String gender = (String)resObj.get("gender");
		    	        
		    	        request.setAttribute("naverCode", naverCode);
		    			request.setAttribute("email",email);
		    			request.setAttribute("name", name);
		    			
		    			System.out.println(naverCode);
		    			dispatch(request, response, "registform2.jsp");
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				//dispatch(request, response, "registform2.jsp");
			}
			
		}else if(command.equals("kakao")) {
			System.out.println("됏따");
			String email = (String)request.getParameter("email");
			System.out.println(email);
			
		}


	}

	private void jsResponse(String msg, String url, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		String res = "<script> alert('" + msg + "'); location.href='" + url + "'; </script>";
		out.print(res);
	}


	private void dispatch(HttpServletRequest request, HttpServletResponse response, String url)
			throws ServletException, IOException {
		RequestDispatcher dispatch = request.getRequestDispatcher(url);
		dispatch.forward(request, response);
	}



}
