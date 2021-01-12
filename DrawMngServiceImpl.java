package com.yura.draw.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import javax.annotation.Resource;
import javax.print.Doc;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sun.star.chart2.SymbolStyle;
import com.sun.star.io.IOException;
import com.yura.common.util.FileVO;
import com.yura.doc.service.DocService;
import com.yura.draw.DrawVO;
import com.yura.draw.service.DrawMngService;
import com.yura.draw.util.Draw3DFileProcess;
import com.yura.draw.util.YuraException;
import com.yura.eco.service.EcoMngService;
import com.yura.part.service.impl.PartMngDAO;
import com.yura.prj.service.PrjService;
import com.yura.prj.service.impl.PrjDAO;
import com.yura.user.service.UserMngService;

import egovframework.com.cmm.LoginVO;
import egovframework.com.cmm.service.EgovProperties;
import egovframework.com.cmm.util.EgovUserDetailsHelper;
import egovframework.com.cop.ems.service.EgovSndngMailRegistService;
import egovframework.com.cop.ems.service.SndngMailVO;
import egovframework.com.utl.fcc.service.EgovFormBasedFileUtil;
import egovframework.com.utl.sim.service.EgovFileScrty;
import egovframework.com.utl.sim.service.EgovFileTool;
import egovframework.rte.fdl.cmmn.EgovAbstractServiceImpl;
import egovframework.rte.fdl.idgnr.EgovIdGnrService;


@Service("DrawMngService")
public class DrawMngServiceImpl extends EgovAbstractServiceImpl implements DrawMngService {
		@Resource(name = "drawMngDAO")
	    private DrawMngDAO drawMngDAO;
	  
		@Resource(name="prjDAO")
	    private PrjDAO prjDAO;

		@Resource(name = "partMngDAO")
		private PartMngDAO partMngDAO;
		
		/** egovDrawManageIdGnrService */
	    @Resource(name="egovDrawManageIdGnrService")
	    private EgovIdGnrService drawIdgenService;
	    
	    @Resource(name="egovDrawFileManageIdGnrService")
	    private EgovIdGnrService drawFileIdgenService;
	    
	    /** egovDrawDistManageIdGnrService */
	    @Resource(name="egovDrawDistManageIdGnrService")
	    private EgovIdGnrService drawDistIdgenService;
		
	    @Resource(name ="DocService")
	    private DocService docService;
	    
	   	@Resource(name = "ecoMngService")
		private EcoMngService ecoMngService;
	    
	   	@Resource(name = "prjService")
		private PrjService prjService;
	   	
	    @Resource(name = "dtrIdGnrService")
	    private EgovIdGnrService dtrIdGnrService;
	    
	    @Resource(name = "distcomOidGnrService")
	    private EgovIdGnrService distcomOidGnrService;
	    
	    @Resource(name="distfileOidGnrService")
	    private EgovIdGnrService distfileOidGnrService;
	    
	    /** egovVerPartManageIdGnrService */
	    @Resource(name="egovVerPartManageIdGnrService")
	    private EgovIdGnrService verPartIdgenService;
	    
	    /** EgovSndngMailRegistService */
		@Resource(name = "sndngMailRegistService")
	    private EgovSndngMailRegistService sndngMailRegistService;
		
		@Resource(name = "userMngService")
		private UserMngService userMngService;
		
		/** yPLM 시스템 URL */
	    private final String systemURL = EgovProperties.getProperty("Globals.Server.Url");

	    /** 첨부파일 위치 지정 */
	    private final String uploadDir = EgovProperties.getProperty("Globals.fileStorePath.draw");
	    
	    /** 첨부파일 Temp폴더 위치 지정 */
	    private final String uploadDirTemp = EgovProperties.getProperty("Globals.fileStorePath.draw.temp");
		
	    /** CAD Check 폴더 위치 지정 */
	    private final String uploadDirCad = EgovProperties.getProperty("Globals.fileStorePath.draw.cad");
	    
	    /**
	     * 도면을 등록한다.
	     *
	     * @param vo
	     * @return
	     * @throws Exception
	     */
	    @Transactional(propagation = Propagation.REQUIRED,rollbackFor={Exception.class})
	    public Map<String, Object> registertDrawInfo(List<FileVO> fileList, HashMap<String, Object> map) throws Exception {
	    	LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
		  	Map<String, Object> result = new HashMap<String, Object>();
		  	List<String> oidList = new ArrayList<String>();
		  	String masterFileName = (String)map.get("masterFilename");
		  	String relmodule = (String)map.get("relmodule");
		  	String oid = "";
	    	if(fileList != null && fileList.size() > 0){
	    		oid = drawIdgenService.getNextStringId();
	    		map.put("oid", oid);
	    		//도면기본정보 등록
	    		map.put("mversion","0");
	    		drawMngDAO.registertDrawInfo(map);
	    		for(int index =0; index<fileList.size(); index++){
	    			HashMap<String,Object> fileParamMap = new HashMap<String,Object>();
	    			String fileOid = drawFileIdgenService.getNextStringId();
					FileVO vo = fileList.get(index);  
					fileParamMap.put("oid", fileOid);
					fileParamMap.put("version", "0");
					fileParamMap.put("modoid", oid);
					fileParamMap.put("humid", loginVO.getId());
					fileParamMap.put("rfilename", vo.getFileName());
					fileParamMap.put("filename", vo.getPhysicalName());
					fileParamMap.put("filesize", vo.getSize());
					fileParamMap.put("ext", vo.getExtName());
					fileParamMap.put("indexno", index+1);
					fileParamMap.put("filepath", vo.getFilePath());
					if(masterFileName != null && masterFileName.equals(vo.getFileName()))
						fileParamMap.put("masterflag", "T");
					else
						fileParamMap.put("masterflag", "");
				    //도면파일정보 등록
				    drawMngDAO.registertDrawFileInfo(fileParamMap);
				}	
	    		
	    	}

	    	//도면연관정보 등록
	    	if(relmodule != null && relmodule.equals("eco")){
	    		Map<String, Object> vo = new HashMap();
	    		vo.put("ecoid", (String)map.get("reloid"));
	    		vo.put("reloid", (String)map.get("oid"));
	    		vo.put("moduletyp", "mod");
	    		
	    		ecoMngService.updateEcRelDrawInfo(vo);
	    	}else{
		    	if(map.get("reloid")!=null && !map.get("reloid").toString().equals(""))
		    		drawMngDAO.registertDrawRelInfo(map);
	    	}
	    	

	    	//도면배포등록
	    	//registerDistInfo(map);
	    	result.put("modoid", oid);
		  	result.put("oidList", oidList);
		  	
	    	return result;
	    }
	
	    /**
	     * 도면첨부파일 저장
	     *
	     * @param vo
	     * @return
	     * @throws Exception
	     */
	    @Transactional(propagation = Propagation.REQUIRED,rollbackFor={Exception.class})
	    public List<String> registerAttachFile(List<FileVO> fileList, DrawVO drawVo) throws Exception {
	    	LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
	    	List<String> oidList = new ArrayList<String>();
	    	HashMap<String,String> map = new HashMap<String,String>();
	    	String oid = "";
	    	try{
	    		//멀티파일 여부체크(T:3D파일경우와 동일하게 폴더안에 파일을 넣는다)
		  		String subasmcheck = (String)map.get("subasmcheck");
		  		
				if(subasmcheck != null && subasmcheck.equals("F")){
		    		for(int index =0; index<fileList.size(); index++){
						
						FileVO vo = fileList.get(index);
						
						oid = drawIdgenService.getNextStringId();
				    	map.put("oid", oid);
				    	map.put("humid", loginVO.getId());
				    	map.put("verprtoid", drawVo.getVerprtoid());
				    	map.put("modkindoid", drawVo.getModkindoid());
				    	map.put("filepath", "");
				    	map.put("rfilename", vo.getFileName());
					    map.put("filename", vo.getPhysicalName());
					    map.put("filesize", vo.getSize()+"");
					    map.put("ext", vo.getExtName());
					    
					    oidList.add(oid);
					    drawMngDAO.registerAttachFile(map);
					}
	    		/*
	    		 * 3D일경우 폴더 생성 후 하나의 정보만 등록
	    		 */	
				}else{
					FileVO vo = fileList.get(0);
					
					oid = drawIdgenService.getNextStringId();
			    	map.put("oid", oid);
			    	map.put("reghumid", loginVO.getId());
			    	map.put("verprtoid", drawVo.getVerprtoid());
			    	map.put("modkindoid", drawVo.getModkindoid());
			    	map.put("filepath", drawVo.getFilepath());
			    	map.put("rfilename", vo.getFileName());
				    map.put("filename", vo.getPhysicalName());
				    map.put("filesize", vo.getSize()+"");
				    map.put("ext", vo.getExtName());
				    
				    oidList.add(oid);
				    drawMngDAO.registerAttachFile(map);
				}
	    	}catch(Exception e){
	    		e.printStackTrace();
	    		 oidList = null;
	    	}
	    	return oidList;
	    }
	    
	    /**
	     *  도면배포 등록 프로세스
	     *
	     * @param vo
	     * @return
	     * @throws Exception
	     */
	    public Object registerDistInfo(HashMap<String, Object> map) throws Exception {
	    	
	    	Object result = null;
	    	if((map.get("distcoms") != null && !map.get("distcoms").equals("")) || (map.get("distteams") != null && !map.get("distteams").equals("")))
		    {
		    	HashMap<String, Object> tempMap = new HashMap<String, Object>();
		    	
		    	String oid = drawDistIdgenService.getNextStringId();
		    	map.put("oid", oid);
		    	map.put("appflag", "N");//결재완료 전에는 'N', 결재 승인 후에는 'Y', 도면등록을 통한 도면배포 등록이 아닐경우 null등록
		    	map.put("title", map.get("dno")+" 도면배포");
		    	map.put("content", "");
		    	
		    	
		    	//1.도면배포 마스터정보 등록
		    	result = drawMngDAO.registertDrawDistInfo(map);

		    	//도면정보와 도면배포OID 연계
		    	tempMap = new HashMap<String, Object>();
		    	tempMap.put("modoid", map.get("modoid"));
		    	tempMap.put("distoid", oid);
		    	drawMngDAO.updateDrawDistOid(tempMap);
		    	
		    	//2.도면배포 배포도면 이력 등록
		    	tempMap.put("oid", oid);
		    	drawMngDAO.registertDistModHistoryInfo(tempMap);
		    	
		    	//3.도면배포 배포팀 이력 등록
		    	if(map.get("distteams") != null && !map.get("distteams").equals("")){
		    		String[] distTeams = map.get("distteams").toString().split("[,]");
		    		for(String distTeam: distTeams){
		    			String[] temp = distTeam.split("[:]");
		    			tempMap.put("distteamoid", temp[0]);
		    			drawMngDAO.registertDistTeamHistoryInfo(tempMap);
		    			
		    			/*
		    			 * 배포팀 메일발송(네오텍) -> 도면결재승인후 메일발송으로 변경
		    			 */
		    			/*List<Map<String, Object>> distTeamInfo = (List<Map<String, Object>>)drawMngDAO.selectDistTeamEmail(tempMap);
		    			if(distTeamInfo != null && distTeamInfo.size()>0){
		    				sendMailDistTeam(distTeamInfo.get(0));
		    			}*/
		    		}
		    	}
		    	
		    	//4.도면배포 협력업체 이력 등록
		    	if(map.get("distcoms") != null && !map.get("distcoms").equals("")){
		    		String[] distComs = map.get("distcoms").toString().split("[,]");
		    		for(String distCom: distComs){
		    			String[] temp = distCom.split("[:]");
		    			tempMap.put("distcomoid", temp[0]);
		    			drawMngDAO.registertDistComHistoryInfo(tempMap);
		    		}
		    	}
	    	}
	    	return result;
	    }

		/*
		 * 메일발송:배포팀에 메일 발송
		 */
		public void sendMailDistTeam(Map<String, Object> resultMap) throws Exception {
			try{
				
					String content = "<br> yPLM 배포도면을 확인하시기 바랍니다.";
					content += "<br><br> 배포팀명:"+ resultMap.get("teamname").toString();
					content += "<br><br> 담당자명:"+ resultMap.get("humname").toString();
					content += "<br><br> 담당자 이메일:"+ resultMap.get("email").toString();
					content += "<br><br><br> ※ 본 메일은 수신 전용입니다.";
//					content += "<br><br><a href='"+systemURL+"' target='_blank'>→ yPLM시스템 바로가기</a>";
	
					//메일 발송
					SndngMailVO sndngMailVO = new SndngMailVO();
			    	sndngMailVO.setDsptchPerson("yPlmMaster");
//			    	sndngMailVO.setRecptnPerson("ddang72@yura.co.kr");//Test용 메일
			    	sndngMailVO.setRecptnPerson(resultMap.get("email").toString());
			    	sndngMailVO.setSj("[yPLM] 배포도면 접수확인 안내");
			    	sndngMailVO.setEmailCn(content);
			    	sndngMailVO.setAtchFileId(""); 
			    	
			    	sndngMailRegistService.insertSndngMailH(null, sndngMailVO);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	    /**
	     * 파트관련 도면체크
	     *
	     * @param vo
	     * @return
	     * @throws Exception
	     */
	    public List<Map<String, Object>> checkPartDrawInfo(DrawVO vo) throws Exception {
	    	return drawMngDAO.checkPartDrawInfo(vo);
	    }
	    
	    /**
	     * 도면버전정보 리스트
	     *
	     * @param vo
	     * @return
	     * @throws Exception
	     */
	    public List<Map<String, Object>> retrievePreDrawList(DrawVO vo) throws Exception {
	    	return drawMngDAO.retrievePreDrawList(vo);
	    }
	    
	    /**
	     * CAD 종류
	     *
	     * @param vo
	     * @return
	     * @throws Exception
	     */
	    public List<Map<String, Object>> selectSftInfo(String iscad) throws Exception {
	    	return drawMngDAO.selectSftInfo(iscad);
	    }
	    
	    /**
	     * 사용자 정보 조회
	     *
	     * @param vo
	     * @return
	     * @throws Exception
	     */
		public List<Map<String, Object>> selecUserListSearching(HashMap<String, Object> map) throws Exception{
			return drawMngDAO.selecUserListSearching(map);
		}
		
	    /**
	     * 사용자 정보 count
	     *
	     * @param vo
	     * @return
	     * @throws Exception
	     */
		public int selecUserListSearchingCnt(HashMap<String, Object> map) throws Exception{
			return drawMngDAO.selecUserListSearchingCnt(map);
		}
		
		/**
		 * 서브파트 여부 체크
		 *
		 * @param verprtoid
		 * @return
		 * @throws Exception
		 */
		public List<Map<String, Object>> checkSubPart(HashMap<String, String> map) throws Exception{
			return drawMngDAO.checkSubPart(map);
		}
		
		/**
		 * 도면 개정정보 리스트
		 *
		 * @param verprtoid
		 * @return
		 * @throws Exception
		 */
		public List<Map<String, Object>>  selectPreDrawList(HashMap<String, String> map) throws Exception{
			return drawMngDAO.selectPreDrawList(map);
		}
		
		/**
		 * 배포담당자 리스트
		 *
		 * @param verprtoid
		 * @return
		 * @throws Exception
		 */
		public List<Map<String, Object>>  userDistSearching(HashMap<String, String> map) throws Exception{
			return drawMngDAO.userDistSearching(map);
		}
		
		/**
		 * 협력업체 리스트
		 *
		 * @param verprtoid
		 * @return
		 * @throws Exception
		 */
		public List<Map<String, Object>>  comDistSearching(HashMap<String, String> map) throws Exception{
			return drawMngDAO.comDistSearching(map);
		}
		
		
		/**
		 * 미결재 리스트
		 *
		 * @param verprtoid
		 * @return
		 * @throws Exception
		 */
		public List<Map<String, Object>>  retrieveApprovalList(HashMap<String, String> map) throws Exception{
			return drawMngDAO.retrieveApprovalList(map);
		}
		
		
		/**
		 * 결재첨부문서 리스트
		 *
		 * @param verprtoid
		 * @return
		 * @throws Exception
		 */
		public List<Map<String, Object>>  retrieveDrawDocList(HashMap<String, String> map) throws Exception{
			return drawMngDAO.retrieveDrawDocList(map);
		}
		
		/**
		 * 입력한 개정번호가 PVS테이블에 존재하는 개정번호인지 체크(PVS테이블:개정관리 테이블)
		 *
		 * @param verprtoid
		 * @return
		 * @throws Exception
		 */
//		public List<Map<String, Object>> retrieveCheckPvs(HashMap<String, Object> map) throws Exception{
//			return drawMngDAO.retrieveCheckPvs(map);
//		}
//		
		/**
		 * 도면정보에 개정번호가 동일한 정보가 있는지 체크
		 *
		 * @param verprtoid
		 * @return
		 * @throws Exception
		 */
		public List<Map<String, Object>> retrieveCheckEqualPvs(HashMap<String, Object> map) throws Exception{
			return drawMngDAO.retrieveCheckEqualPvs(map);
		}
		
		
		/**
		 * 도면정보 결재상태 체크
		 *
		 * @param verprtoid
		 * @return
		 * @throws Exception
		 */
		public List<Map<String, Object>> retrieveCheckStatus(HashMap<String, Object> map) throws Exception{
			return drawMngDAO.retrieveCheckStatus(map);
		}
	    
	    /**
	     * 도면indexno
	     *
	     * @param map
	     * @return
	     * @throws Exception
	     */
	    public int retrieveIndexNo(HashMap<String, Object> map) throws Exception {
	    	return drawMngDAO.retrieveIndexNo(map);
	    }
	    
	    /**
	     * 도면 pvs check
	     *
	     * @param map
	     * @return
	     * @throws Exception
	     */
	    public List<Map<String, Object>> retrieveCheckPvs(HashMap<String, Object> map) throws Exception {
	    	return drawMngDAO.retrieveCheckPvs(map);
	    }
	    

		/**
		 * 도면첨부파일 리스트
		 *
		 * @param modoidList
		 * @return
		 * @throws Exception
		 */
		public List<Map<String, Object>> selectModFileList(String[] oidList) throws Exception{
			HashMap<String, String> map = new HashMap<String, String>();
			StringBuffer sql = new StringBuffer();
			
			sql.append("SELECT 	ROW_NUMBER() OVER (ORDER BY oid ASC) AS NO, OID, SFTOID, FILEPATH, substring(filename from position('.' in filename) for 5) FILETYPE, ");
			sql.append("		FILENAME, RFILENAME, FILESIZE, '완료' STATUS, REGHUMID, VERPRTOID, MODKINDOID, REGDATE ");
			sql.append("FROM MOD ");
			sql.append("WHERE OID IN ('' ");
			for(String oid : oidList){
				sql.append(",");
				sql.append("'"+oid+"'");
			}
			sql.append(") ");
			sql.append("ORDER BY OID ");
			
			map.put("strSql", sql.toString());
			return drawMngDAO.selectModFileList(map);
		}

		/**
		 * 도면개정 리스트
		 *
		 * @param oid, name
		 * @return
		 * @throws Exception
		 */
		public List<Map<String, Object>> selectPvsInfo(HashMap<String,  Object> map) throws Exception{
			return drawMngDAO.selectPvsInfo(map);
		}

		/**
		 * 도면상세 정보
		 */
		public List<Map<String, Object>> retrieveDrawInfo(HashMap<String, Object> map) throws Exception{

			return drawMngDAO.retrieveDrawInfo(map);
		}
		
		/**
		 * 단위도면 도면상세 정보
		 */
		public List<Map<String, Object>> retrieveUnitDrawingInfo(HashMap<String, Object> map) throws Exception{
	/*		String pvsoid = "";
			//A0 일단 조회하여 context에 PVSOID이름으로 담아둔다
			List<Map<String, Object>> pvsInfo = drawMngDAO.selectPvsInfo(map);
			if(pvsInfo != null && pvsInfo.size() > 0)
			{
				pvsoid = (String)pvsInfo.get(0).get("OID");
			}
			
			//AA 조건(pno,pvsname)으로 단위파트존재 확인
			drawMngDAO.selectUnitDrawingPrt(map);
			
			//processGroup AB 단위파트 추가(단위도면 파트정보가 존재하지 않을 경우 수행) 
			//ABA
			drawMngDAO.selectUnitDrawingPrt(map);
			
			//ABB
			drawMngDAO.selectUnitDrawingPrt2(map);
			
			//ABC  processGroup
			//ABCA 파트체크, 기존에 사내도 정보가 PRT에 존재하지 않으면 process1 프로세스 수행
			drawMngDAO.selectDrawingPrt2(map);
			
			//ABCB
			drawMngDAO.registerXUnitPart(map);
			
			//ABCC 파트추가(VERPRT) 
			drawMngDAO.registerXUnitVerPart(map);
			
			//ABD processGroup:
			//ABDA 파트체크
			//기존에 단위도면 정보가 PRT에 존재하면 process2 프로세스 수행
			drawMngDAO.selectDrawingPrt2(map);
			
			//ABDB 버젼OID
			drawMngDAO.selectPvsInfo(map);
			
			//ABDC 파트추가(VERPRT) 
			drawMngDAO.registerXUnitVerPart(map);
			
			// AC 단위도면 정보 MOD 테이블에 등록: 단위도면 파트 정보를 mod 테이블에 삽입
			drawMngDAO.registerDrawing(map);
			
			// AD 단위도면 정보 PSM 테이블에 등록, 단위도면 정보와 부모파트 정보를 연결시켜주는 PSM 테이블에 삽입
			drawMngDAO.registerXunitPsm(map);
			
			// AE 해당 단위도면의 파트정보(verprt)를 최신버젼은 'T'로 나머지는 'F'로 변경
			drawMngDAO.registerPrtMaxVersion(map);
			
			// AF 해당 단위도면정보(mod)를 최신버젼은 'T'로 나머지는 'F'로 변경
			drawMngDAO.registerUnitDrawMaxVersion(map);
			
			//AG 도면 정보 DRAWREL 테이블에 등록.. 인데 단위도면은 DRAWREL과 관계없어서 나중에 제외할것임
			drawMngDAO.registerDrawRelationInfo(map);
		*/	
			return drawMngDAO.retrieveDrawInfo(map);
		}
		
		/**
		 * 도면체크아웃
		 */
		public int updateCheckOut(HttpServletRequest request, HashMap<String, Object> map) throws Exception{
			return drawMngDAO.updateCheckOut(map);
		}
		public int updateModFileCheckOut(HttpServletRequest request, HashMap<String, Object> map) throws Exception{
			return drawMngDAO.updateModFileCheckOut(map);
		}
		/**
		 * 도면체크인
		 */
		public int updateCheckIn(HttpServletRequest request, HashMap<String, Object> map) throws Exception{
			return drawMngDAO.updateCheckIn(map);
		}
		
		/**
		 * CAD 도면체크인시 개정 업데이트
		 */
		public String updateCheckInCAD(HashMap<String, Object> map, List<Map<String, Object>> chgFileList) throws Exception{
			String newoid = "";
			try{
				List<Map<String, Object>> fileList = drawMngDAO.selectSubAsmList(map);
				List<Map<String, Object>> relBomList = drawMngDAO.selectRelEbomInfo(map);
				String mversion = (String)map.get("mversion");
				String mreversion = (String)map.get("mreversion");
				String modoid = (String)map.get("modoid");
				String humid = (String)map.get("humid");
				if(mreversion != null && !mreversion.equals(mversion)){
					newoid = drawIdgenService.getNextStringId();
		    		map.put("oid", newoid);
		    		map.put("mversion", mreversion);
		    		drawMngDAO.registertDrawInfo(map);
		    		if(fileList != null){
		    			for(int f=0; f < fileList.size(); f++){
			    			Map fileMap = (Map)fileList.get(f);
			    			HashMap<String,Object> fileParamMap = new HashMap<String,Object>();
							String rfilename = (String)fileMap.get("rfilename");
							String mfilename = (String)fileMap.get("filename");
							if(chgFileList != null){
								for(int c = 0; c < chgFileList.size(); c++){
									Map chgFileMap = (Map)chgFileList.get(c);
									String chgRFileNam =  (String)chgFileMap.get("rfilename");
									String chgFileNam =  (String)chgFileMap.get("filename");
									if(chgRFileNam != null && chgRFileNam.equals(rfilename)){
										mfilename = chgFileNam;
									}
								}
							}
							String masterflag = (String)fileMap.get("mk");
							BigDecimal indexno = (java.math.BigDecimal) fileMap.get("indexno");
							fileParamMap.put("modoid", newoid);
							fileParamMap.put("rfilename", rfilename);
							fileParamMap.put("filename", mfilename);
							fileParamMap.put("filesize", 0);
							fileParamMap.put("indexno", indexno);
							fileParamMap.put("masterflag", masterflag);
						    //도면파일정보 등록
						    drawMngDAO.registertDrawFileInfo(fileParamMap);
		    			}
		    		}
		    		//EBOM 등록 처리
		    		if(relBomList != null){
						for(int i = 0; i < relBomList.size(); i++){
							HashMap<String, Object> embomParamMap = new HashMap<String, Object>();
							Map relBomMap = (Map)relBomList.get(i);
							embomParamMap.put("modoid", (String)relBomMap.get("modoid"));
							embomParamMap.put("parentoid", newoid);
							embomParamMap.put("seq", String.valueOf(relBomMap.get("seq")));
							embomParamMap.put("humid", humid);
							drawMngDAO.registertEbomInfo(embomParamMap);
						}
		    		}
				}
				/*
				//도면연관정보 등록
				if(map.get("reloid")!=null && !map.get("reloid").toString().equals("")){
					drawMngDAO.registertDrawRelInfo(map);
				}
				*/
				int checkin = drawMngDAO.updateCheckIn(map);
			}catch(Exception e){
				e.printStackTrace();
			}
			return newoid;
		}
		
		/**
		 * 도면수정
		 */
		public int updateDrawInfo(HttpServletRequest request, HashMap<String, Object> map) throws Exception{
			return (Integer)drawMngDAO.updateDrawInfo(map);
		}
		
		/**
		 * 도면삭제
		 */
		@Transactional
		public int deleteDrawInfo(HttpServletRequest request, HashMap<String, Object> map) throws Exception{
			int resultCnt  = 0 ;

			String prevModoid = "";
    		List<Map<String, Object>> prevDnoInfo = (List<Map<String, Object>>)drawMngDAO.selectPrevDno(map);
    		if(prevDnoInfo != null && prevDnoInfo.size() == 1){
    			Map prevDnoInfoMap = (Map)prevDnoInfo.get(0);
    			prevModoid =  (String)prevDnoInfoMap.get("oid");
    		}
			// 도면 정보 삭제
			resultCnt = drawMngDAO.deleteDrawInfo(map);
			drawMngDAO.deleteDrawEbomInfo(map);
    		List<Map<String, Object>> ebomInfo = (List<Map<String, Object>>)drawMngDAO.selectRootOidList(map);
    		if(ebomInfo != null && ebomInfo.size() > 0 && prevModoid != null && !prevModoid.equals("")){
    			for(int i=0; i < ebomInfo.size(); i++){
    				Map ebomInfoMap = (Map)ebomInfo.get(i);
    				String parentoid = (String)ebomInfoMap.get("parentoid");
    				String modoid = (String)ebomInfoMap.get("modoid");
    				HashMap<String, Object> upParamMap = new HashMap();
    				upParamMap.put("parentoid", parentoid);
    				upParamMap.put("modoid", modoid);
    				upParamMap.put("chgmodoid", prevModoid);
    				drawMngDAO.updateReturnEbomInfo(upParamMap);
    			}
    		}else{
    			drawMngDAO.deleteDrawEbomInfo2(map);
    		}
    		
			
			// 도면 파일 정보 리스트
			List<Map<String, Object>> drawFileInfo = drawMngDAO.selectDrawInfoBeforeDelete(map);
			if(drawFileInfo != null && drawFileInfo.size() > 0 ){
				for(int i = 0; i < drawFileInfo.size(); i++){
					Map drawFileInfoMap = (Map)drawFileInfo.get(i);
					String oid = (String)drawFileInfoMap.get("oid");
					String version = (String)drawFileInfoMap.get("version");
					String filename = (String)drawFileInfoMap.get("filename");
					String filepath = uploadDir + File.separator + filename;
					File drawFile  =  new File(filepath);
					if(drawFile.exists()){
						EgovFileTool.deleteFile(filepath);
					}
					
					map.put("oid", oid);
					map.put("version", version);
					// 도면 파일 정보 삭제
					drawMngDAO.deleteDrawFilesInfo(map);
				}			
			}
			
			//도면 연계 정보 리스트
			List<Map<String, Object>> drawRelInfo = (List<Map<String, Object>>)drawMngDAO.selectRelMoudleInfo(map);
			if(drawRelInfo != null && drawRelInfo.size() > 0 ){
				for(int j = 0; j < drawRelInfo.size(); j++){
					Map drawRelInfoMap = (Map)drawRelInfo.get(j);
					String reloid = (String)drawRelInfoMap.get("reloid");
					if(reloid != null && reloid.indexOf("ECH") != -1){
						HashMap<String, Object> ecRelParamMap = new HashMap();
						ecRelParamMap.put("ecoid", (String)drawRelInfoMap.get("reloid"));
						ecRelParamMap.put("reloid", (String)drawRelInfoMap.get("modoid"));
						ecoMngService.deleteEcRelation(ecRelParamMap);
					}else if(reloid != null && reloid.indexOf("VDO") != -1){
						HashMap<String, String> docRelParamMap = new HashMap();
						docRelParamMap.put("verdocoid", (String)drawRelInfoMap.get("reloid"));
						docRelParamMap.put("modoid", (String)drawRelInfoMap.get("modoid"));
						drawMngDAO.deleteDrawDocInfo(docRelParamMap);
					}else{
						List<Map<String, Object>> result = (List<Map<String, Object>>)drawMngDAO.selectRelexistother(drawRelInfoMap);
						if (result == null || result.size() <= 0){
							drawRelInfoMap.put("prjdiv", drawRelInfoMap.get("reloid").toString().substring(0, 3));
							
							//reloid에 속한 다른 도면이 있으면 패스, 없으면 프로젝트 작업상태를 대기로 변경
							drawMngDAO.updatePrjSta(map);
							
							//관련 작업산출물 결재정보 삭제
							drawMngDAO.deletePrjApprove(map);
							
							//10.프로젝트 모듈에 연계된 도면이 모두 삭제된 경우 PRJSTAGE.STARTDATE갱신
							drawMngDAO.updatePrjStageDel(map);
						}
						//1-3. drawrel테이블의 해당 도면관련 데이타 삭제, modoid, reloid 로 조회하여 삭제
					}
				}
				resultCnt = drawMngDAO.deleteRelOwn(map);  
			 }
			 return resultCnt;
		}
		
		/**
		 * 도면 모듈에서 PVS OID 가져오기.
		 */
		public Object retrieveCheckDrawPvs(HashMap<String, Object> map) throws Exception{
			//Object pvsoid = "FFF";
			Object pvsoid = "";
			//1.파트 버전 OID값을 PVS 테이블에서 추출
			/*
			List<Map<String, Object>> relInfo = drawMngDAO.retrieveCheckPvs(map);
			if (relInfo != null && relInfo.size() > 0){
				Map<String, Object> relMap = relInfo.get(0);
				pvsoid = relMap.get("oid");
			}
			*/
			
			//2.파트 버전 OID값을 PVS 테이블에서 추출
			List<Map<String, Object>> relInfo2 = drawMngDAO.retrieveCheckEqualPvs(map);
			if (relInfo2 != null && relInfo2.size() > 0){
				pvsoid = "isEXIST";
			}
			
			/*
			//3. 파트 버전 OID값을 PVS 테이블에서 추출
			List<Map<String, Object>> relInfo3 = drawMngDAO.retrieveCheckEqualPvs(map);
			if (relInfo3!= null && relInfo3.size() > 0){
				pvsoid = "ingEXIST";
			}
			*/
			
			return pvsoid;
		}
		
		public Object retrieveMaxDrawCheck(HashMap<String, Object> map) throws Exception{
			String result = "";
			String prevMversion = (String)map.get("mversion");
			String mversion = "";
			List<Map<String, Object>> drawInfo = drawMngDAO.retrieveMaxDrawCheck(map);
			if (drawInfo != null && drawInfo.size() > 0){
				mversion = String.valueOf(drawInfo.get(0).get("mversion"));
				if(Integer.parseInt(prevMversion) == Integer.parseInt(mversion)){
					result = "Success";
				}else{
					result = "FFF";
				}
			}
			return result;
		}

		
		/**
		 * 도면 3D파일 목록 가져오기
		 */
		public List<Map<String, Object>> selectSubAsmList(HttpServletRequest request, HashMap<String, Object> map) throws Exception{
			
			String realPath = (String)map.get("uploadDir");
			List<Map<String, Object>> assemFileList = null;
			
			//3D 도면일때
			if(map.get("mkoid")!=null && map.get("mkoid").toString().equals("")){
				List<Map<String, Object>> results = drawMngDAO.selectSubAsmList(map);
				map.put("results", results);
			
				Draw3DFileProcess file = new Draw3DFileProcess(request, realPath);
				assemFileList = file.getAssembleFileList(map);
			}
			return assemFileList;
		}

		/**
	    * 단일도면 파일추가 등록
	    */
		public int uploadAdditionFile2D(HashMap<String, Object> map) throws Exception{
			return drawMngDAO.updateDrawFileInfo(map);
		}

		 /**
	     * 멀티 도면파일추가 
	     */
		public int uploadAdditionFile3D(List<FileVO> file, HashMap<String, Object> map) throws Exception{
			int result = 0;
			if(file.size()>0){
				result = registerAsmFileInfo(file, map);
			}
			return result;
		}

		/**
		 * 도면파일추가 삭제
		 */
		public int deleteDrawFile(HttpServletRequest request, HashMap<String, Object> map) throws Exception{
			
			String fileName = (String)map.get("filename");
			String uploadDir = (String)map.get("uploadDir");
			
			if (!deleteFile(uploadDir, fileName))
			{
				throw new Exception("deletion file don't success!");
			}
			map.put("filename", "");
			map.put("dbfilename", "");
			
			int result = drawMngDAO.updateDrawFileInfo(map);
			return result;
		}
		
		/**
		 * 도면 파일정보 수정
		 */
		public int updateDrawFileInfo(HashMap<String, Object> map) throws Exception{
			return drawMngDAO.updateDrawFileInfo(map);
		}
		
	    /**
	     * 도면개정리스트
	     */
	    public List<Map<String, Object>> retrieveDrawVersionList(HashMap<String, Object> map) throws Exception{
	    	 return drawMngDAO.retrieveDrawVersionList(map);
	    }
	    
	    /**
	     * 도면과 연결된 도면리스트
	     */
	    public List<Map<String, Object>> retrieveDrawRelDrawList(HashMap<String, Object> map) throws Exception{
	    	 return drawMngDAO.retrieveDrawRelDrawList(map);
	    }
	    
	    /**
	     * ERP BOM리스트
	     */
	    public List<Map<String, Object>> retrieveUnitDrawList(HashMap<String, Object> map) throws Exception{
	    	return drawMngDAO.retrieveUnitDrawList(map);
	    }
	    
	    /**
	     * 설변정보 리스트
	     */
	    public List<Map<String, Object>> retrieveEcPartList(HashMap<String, Object> map) throws Exception{
	    	return drawMngDAO.retrieveEcPartList(map);
	    }
	    
		/*** 해당 도면과 관련된 프로젝트 모듈의 정보를 가져오는 클래스
		 */
		public List<Map<String, Object>> retrieveRelationModuleInfo(HashMap<String, Object> map) throws Exception{
			
			List<Map<String, Object>> result = null;
			List<Map<String, Object>> relResults = (List<Map<String, Object>> )drawMngDAO.selectRelationModuleOid(map);
			
			// 관련 모듈의 OID값이 WKO, WKI일 경우에만 프로세스 수행
			if (relResults.size() > 0)
			{
				Map<String, Object> tmpMap = relResults.get(0);
				
				String relOID = (String) tmpMap.get("rel");
					
					if (relOID.startsWith("WKO") || relOID.startsWith("WKI"))
					{
						
						StringBuffer sb = new StringBuffer();
						sb.append(" SELECT DISTINCT prj.name AS prjname, prj.oid AS prjoid, prjstage.name AS prjstagename, ");
						sb.append(" prjwork.stageoid, prjwork.workoid, prjstage.content, hum.name AS worker, prjstage.revision ");
						sb.append(" FROM hum, prjwork, prjstage, prj, ");
						sb.append(" (SELECT   OID, MAX (revision) AS revision FROM prjstage GROUP BY OID) temp ");
						sb.append(" WHERE prjstage.prjoid = prj.oid ");
						sb.append(" AND prjwork.stageoid = prjstage.oid ");
						sb.append(" AND hum.id = prjstage.humid ");
						sb.append(" AND prjstage.OID = temp.OID  ");
						sb.append(" AND prjstage.revision = temp.revision ");
						sb.append(" AND prjwork.workoid IN ( ");
						
						boolean flag = true;
						for (Map<String, Object> tmpMap2 : relResults) {
							if (flag)	sb.append(" '"+tmpMap2.get("reloid") +"' ");
							else		sb.append(" , '"+tmpMap2.get("reloid") +"' ");
							
							flag = false;
						}
						
						sb.append(" ) ");
						sb.append(" ORDER BY revision DESC ");
						
						map.put("strSql", sb.toString());
						result = drawMngDAO.selectProjectModuleInfo(map);
					}
				}

			return result;
		}
		
		/*
		 * 파일이름 변경
		 */
		private boolean renameTo(String filePath, String currentFileName, String afterFileName) {
			
			File afterFile = new File(filePath + File.separator + afterFileName);
			
			if (afterFile.exists())
			{
				afterFile.delete();
			}
			
			File currentFile = new File(filePath + File.separator + currentFileName);
		
			return currentFile.renameTo(afterFile);
		}	
		
		/*
		 * 파일삭제
		 */
		private boolean deleteFile(String filePath, String fileName) {
			File file = new File(filePath + File.separator + fileName);
			
			if (file.exists())
			{
				return file.delete();
			}
			
			return true;
		}

		@Override
		public int retrieve(HttpServletRequest request,
				HashMap<String, Object> map) throws Exception {
			// TODO Auto-generated method stub
			return 0;
		}
		
		/*
		 * 문서삭제 
		 */
		public int deleteDocInfo(HashMap<String, String> map) throws Exception {
			int resultCnt = 0;
			List<Map<String, Object>> results = (List<Map<String, Object>>)drawMngDAO.selectDrawDocRefInfo(map);
			String serverRoot = (String)map.get("serverRoot");
		    String uploadDir = EgovProperties.getProperty("Globals.fileStorePath.doc");
			
			for (int i = 0; i < results.size() ; i++) {
				Map<String, Object> result = (Map<String, Object>)results.get(i);
				map.put("verdocoid", (String)result.get("verdocoid"));
				map.put("docoid", (String)result.get("docoid"));
				map.put("ref", (String)result.get("ref"));
				map.put("docVerNum", (String)result.get("docvernum"));
				
				//도면에서 문서추가하여 등록한 문서인경우만 문서정보 자체를 삭제(문서가 작업중이거나 반송인 경우에만 삭제)
				if(map.get("ref") != null && !"".equals(map.get("ref").toString()) && "F".equals(map.get("ref").toString()) 
						&& (result.get("staoid").toString().equals("CCN00192") 	|| result.get("staoid").toString().equals("CCN00197"))) {
						docService.deleteDocInfo(map, serverRoot, uploadDir); 
				}
				//docrel정보에서 관련정보 삭제
				resultCnt = drawMngDAO.deleteDrawDocInfo(map);
			}
			
			return resultCnt;
		}
		
		/*
		 * 문서선택 
		 */
		public int insertDrawDocRel(HashMap<String, String> map) throws Exception {
			int resultCnt = 0;
			List<Map<String, Object>> docInfo = docService.selectDrawModuleInfo(map);
			if(docInfo != null && docInfo.size()>0){
				//도면 문서정보 수정
				drawMngDAO.updateDrawDocRel(map);
				resultCnt = 0;
			}else{
				//관련문서정보 등록
				docService.insertDocRelation(map);
				resultCnt = 1;
			}
			return resultCnt;
		}

		/**
	     * 파트리스트(CAD I/G용)
	     *
	     * @param vo
	     * @return
	     * @throws Exception
	     */
		public List<Map<String, Object>> retrievePartSearchList(HashMap<String, Object> map) throws Exception{
			map.put("strSql", getSql("prt", map));
			return drawMngDAO.retrievePartSearchList(map);
		}
	
		/**
		 * 3D파일일 경우 폴더안의 파일정보 등록
		 * @param vo
	     * @return
	     * @throws Exception
	     */
		public int registerAsmFileInfo(List<FileVO> fileList, HashMap<String, Object> paramMap) throws Exception{
			
			int result = 0;
			HashMap<String, Object> map;
			String[] distDraws = null;
			if(paramMap.get("distdraws") != null && !paramMap.get("distdraws").equals("")){
	    		distDraws = paramMap.get("distdraws").toString().split("[,]");
			}
    		for(int index =0; index<fileList.size(); index++){
				FileVO vo = fileList.get(index);
				
				map = new HashMap<String, Object>();
		    	map.put("modoid", paramMap.get("modoid"));
			    map.put("filename", vo.getFileName());
			    map.put("filepath", "");
			    map.put("filesize", vo.getSize()+"");
			    map.put("ext", vo.getExtName());
			    map.put("seq", index+1);
			    map.put("distchk", "F");
			    if(distDraws != null && distDraws.length>0){
				    for(String distDraw: distDraws){
		    			if(distDraw.equals(vo.getFileName())){
		    				 map.put("distchk", "T");
		    			}
		    		}
			    }
		    	drawMngDAO.registerAsmFileInfo(map);
			}	
    		return result;
		} 
		
		/**
		 * 3D파일일 경우 폴더안의 파일정보 삭제
		 * @param vo
		 * @return
		 * @throws Exception
		 */
		public int deleteAsmFileInfo(HashMap<String, Object> map) throws Exception{
			return	(Integer)drawMngDAO.deleteAsmFileInfo(map);
		} 
		
		/**
		 * AsmFile 배포정보 수정
		 * @param vo
		 * @return
		 * @throws Exception
		 */
		public int updateDistAsmFile(List<Map<String, Object>> map) throws Exception{
			return	(Integer)drawMngDAO.updateDistAsmFile(map);
		} 
		
		/**
		 * 3D파일일 경우 폴더안의 파일정보 등록(CAD I/G)
		 * @param vo
		 * @return
		 * @throws Exception
		 */
		public int registertAsmFileCADInfo(HashMap<String, Object> map) throws Exception{
			int result = 0;
			Object fileLists = map.get("fileList");
			String[] fileList = fileLists.toString().split("[,]");
			for(int index =0; index<fileList.length; index++){
	    		String tempFilePath = fileList[index].substring(0, fileList[index].lastIndexOf("/"));
	    		String tempFileName = fileList[index].substring(fileList[index].lastIndexOf("/")+1, fileList[index].length());
	    		String ext = tempFileName.substring(tempFileName.lastIndexOf(".")+1, tempFileName.length());
	    		
				map.put("modoid", map.get("modoid"));
				map.put("filename", tempFileName);
				map.put("dbfilename", tempFileName);
				map.put("filepath", tempFilePath);
				map.put("filesize", "");
				map.put("ext", ext);
				map.put("seq", index+1);
				map.put("distchk", "F");
				 
				drawMngDAO.registerAsmFileInfo(map);
				result++;
			}	
			return result;
		} 
		
		/**
		 * 도면 파일리스트
		 *
		 * @param vo
		 * @return
		 * @throws Exception
		 */
		public List<Map<String, Object>>  retrieveAsmFileInfo(Map<String, Object> map) throws Exception{
			return drawMngDAO.retrieveAsmFileInfo(map);
		}

		/**
		 * 도면리스트(CAD I/G용)
		 *
		 * @param vo
		 * @return
		 * @throws Exception
		 */
		public List<Map<String, Object>> retrieveDrawSearchList(HashMap<String, Object> map) throws Exception{
			map.put("strSql", getSql("mod", map));
			return drawMngDAO.retrieveDrawSearchList(map);
		}
		
		/**
		 * 프로젝트리스트(CAD I/G용)
		 *
		 * @param vo
		 * @return
		 * @throws Exception
		 */
		public List<Map<String, Object>> retrievePrjectSearchList(HashMap<String, Object> map) throws Exception{
			map.put("strSql", getSql("prj", map));
			return drawMngDAO.retrievePrjectSearchList(map);
		}

		/**
		 * 3D 파일일 경우 폴더 생성하기 위한 도면폴더순서
		 *
		 * @param vo
		 * @return
		 * @throws Exception
		 */
		public Object retrieveDirSeq(HashMap<String, Object> map) throws Exception{
			return drawMngDAO.retrieveDirSeq(map);
		}

	    /**
	     * 도면(draw)를 등록(CAD I/G용)
	     *
	     * @param vo
	     * @return
	     * @throws Exception
	     */
	    @Transactional(propagation = Propagation.REQUIRED,rollbackFor={Exception.class})
	    public String registertDrawInfoCAD(List<FileVO> fileList, HashMap<String, Object> map) throws Exception {
	    	String oid = "";
	    	if(fileList != null && fileList.size() > 0){
	    		oid = drawIdgenService.getNextStringId();
	    		map.put("oid", oid);
	    		//도면기본정보 등록
	    		drawMngDAO.registertDrawInfo(map);
	    		String filename = (String)map.get("filename");
	    		for(int index =0; index<fileList.size(); index++){
	    			HashMap<String,Object> fileParamMap = new HashMap<String,Object>();
					FileVO vo = fileList.get(index);  
					if(filename != null && filename.indexOf(vo.getFileName()) != -1)
						fileParamMap.put("masterflag", "T");
					fileParamMap.put("modoid", oid);
					fileParamMap.put("humid", map.get("id"));
					fileParamMap.put("rfilename", vo.getFileName());
					fileParamMap.put("filename", vo.getPhysicalName());
					fileParamMap.put("filesize", vo.getSize());
					fileParamMap.put("ext", vo.getExtName());
					fileParamMap.put("indexno", index+1);
					
				    //도면파일정보 등록
				    drawMngDAO.registertDrawFileInfo(fileParamMap);
				}	
	    		
	    	}
	    	//도면연관정보 등록
	    	if(map.get("reloid")!=null && !map.get("reloid").toString().equals(""))
	    		drawMngDAO.registertDrawRelInfo(map);

		  	return oid;
	    }
		
		/**
		 * 로그인체크(CAD I/G용)
		 *
		 * @param vo
		 * @return
		 * @throws Exception
		 */
	    public List<Map<String, Object>> userLoginCheck(HashMap<String, Object> map) throws Exception{
	    	return drawMngDAO.userLoginCheck(map);
	    }
	    
	    /**
	     * 파트카테고리 리스트(CAD I/G)를 등록한다.
	     *
	     * @return
	     * @throws Exception
	     */
	    @Transactional(propagation = Propagation.REQUIRED,rollbackFor={Exception.class})
	    public List<Map<String, Object>> selectCatPrtList(HashMap<String, Object> map) throws Exception {
	    	return drawMngDAO.selectCatPrtList(map);
	    }
	    
	    /**
	     * 엔진카테고리 리스트(CAD I/G)를 등록한다.
	     *
	     * @return
	     * @throws Exception
	     */
	    public List<Map<String, Object>> selectCatEngList(HashMap<String, Object> map) throws Exception {
	    	return drawMngDAO.selectCatEngList(map);
	    }
	    
	    /**
	     * 3D파일 리스트 조회, AssembleList
	     *
	     * @return
	     * @throws Exception
	     */
	    public List<Map<String, Object>> retrieveSubAssemblyInfo(HashMap<String, Object> map) throws Exception {
	    	return drawMngDAO.selectSubAsmList(map);
	    }
	    
	    public List<Map<String, Object>> retrieveSubHistAssemblyInfo(HashMap<String, Object> map) throws Exception{
	    	return drawMngDAO.selectSubHistAsmList(map);
	    }
	   
	 
	    /*
	     * 개정생성
	     */
	    @Transactional(propagation = Propagation.REQUIRED,rollbackFor={Exception.class})
	    public Map<String, Object> insertNewVersion(HashMap<String, Object> context) throws Exception{
	    	/*
	    	 * 1.개정생성 데이터 등록 및 기존개정의 파일 복사
	    	 * <ref bean="drawing.insert.newDrawingVersionFileInfo" />
	    	 */
	    	LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
			Map<String, Object> result = new HashMap<String, Object>();
	    	String resultMsg = "";
			String drawFile = "";
			String changedFileName = "";	
			String changedRFileName = "";	
			String previousFilePath = "";
			String filePath = (String)context.get("uploadDir");
			boolean exeFlag = true;
			String modOID = "";
			List<Map<String, Object>> drawInfo = drawMngDAO.retrieveDrawInfo(context);
			String mversion = context.get("mversion") != null ? String.valueOf(Integer.parseInt((String)context.get("mversion"))+1) : "";
			if(drawInfo != null && drawInfo.size() > 0 && mversion != null && !mversion.equals("")){
				Map<String, Object> drawInfoMap = (Map)drawInfo.get(0);
				HashMap<String, Object> paramMap = new HashMap();
				modOID = drawIdgenService.getNextStringId();
				String caroid = (String)drawInfoMap.get("caroid");
				paramMap.put("modoid", modOID);
				paramMap.put("oid", modOID);
				paramMap.put("engctgoid",caroid);
				paramMap.put("prttypeoid",(String)drawInfoMap.get("prttypeoid"));
				paramMap.put("moduletype",(String)drawInfoMap.get("moduletype"));
				paramMap.put("dno",(String)drawInfoMap.get("dno"));
				paramMap.put("dnam",(String)drawInfoMap.get("dnam"));
				paramMap.put("dscoid",(String)drawInfoMap.get("dscoid"));
				paramMap.put("mversion", mversion);
				paramMap.put("eono",(String)drawInfoMap.get("eono"));
				paramMap.put("modtypeoid",(String)drawInfoMap.get("modtypeoid"));
				paramMap.put("modsizeoid",(String)drawInfoMap.get("modsizeoid"));
				paramMap.put("devstep",(String)drawInfoMap.get("devstep"));
				paramMap.put("disthumid","");
				paramMap.put("humid",loginVO.getId());
				//1.개정등록
				drawMngDAO.registertDrawInfo(paramMap);
				List<Map<String, Object>> drawFileInfo = drawMngDAO.selectSubAsmList(context);
				if(drawFileInfo != null && drawFileInfo.size() > 0){
					for(int i = 0; i < drawFileInfo.size(); i++){
						Map<String, Object> drawFileInfoMap = (Map)drawFileInfo.get(i);
						HashMap<String, Object> fileParamMap = new HashMap();
						String fileOid = drawFileIdgenService.getNextStringId();
						String filename = (String)drawFileInfoMap.get("filename");
						String fileExt = filename.substring(filename.indexOf(".")+1, filename.length());
						String physicalName = EgovFormBasedFileUtil.getPhysicalFileName()+"."+fileExt;
						String rfilename = (String)drawFileInfoMap.get("rfilename");
						String filepath = (String)drawFileInfoMap.get("filepath");
						fileParamMap.put("oid", fileOid);
						fileParamMap.put("filename", physicalName);
						fileParamMap.put("rfilename", rfilename);
						fileParamMap.put("filepath", filepath);
						fileParamMap.put("version", "0");
						fileParamMap.put("filesize", (java.math.BigDecimal)drawFileInfoMap.get("filesize"));
						fileParamMap.put("indexno", (java.math.BigDecimal)drawFileInfoMap.get("indexno"));
						fileParamMap.put("masterflag", (String)drawFileInfoMap.get("mk"));
						fileParamMap.put("modoid", modOID);
						if (!copyTo2(uploadDir + filepath + File.separator + filename, uploadDir + filepath + File.separator + physicalName)){
							resultMsg = "NewDrawVersion file is not exist! drawFile : "+filename +" changedFileName :"+physicalName;
						} 
						//2.개정 파일복사 등록
					    drawMngDAO.registertDrawFileInfo(fileParamMap);
					}
				}
				// 3.EBOM 정보등록
				List<Map<String, Object>> drawEbomInfo = drawMngDAO.selectRelEbomInfo(context);
				if(drawEbomInfo != null && drawEbomInfo.size() > 0){
					for(int d = 0; d < drawEbomInfo.size(); d++){
						Map<String, Object>drawEbomInfoMap = (Map)drawEbomInfo.get(d);
						HashMap<String, Object> ebomParamMap = null;
						ebomParamMap = new HashMap();
						ebomParamMap.put("modoid", drawEbomInfoMap.get("modoid"));
						ebomParamMap.put("parentoid", modOID);
						ebomParamMap.put("seq", String.valueOf(drawEbomInfoMap.get("seq")));
						ebomParamMap.put("humid", loginVO.getId());					
						drawMngDAO.registertEbomInfo(ebomParamMap);
					}
				}
				
				//4.파트 연결 (파트 최신버전으로 연결)
				paramMap.put("oldmodoid", context.get("modoid"));
				paramMap.put("moid", context.get("modoid"));
				List<Map<String, Object>> relInfoList = drawMngDAO.selectRelationModuleOid(paramMap);
				List relInfoArrayList = new ArrayList();
				LinkedHashSet relInfoTemp =new LinkedHashSet<HashMap<String, String>>();
				if(relInfoList != null && relInfoList.size() > 0){
					for(int r = 0; r < relInfoList.size(); r++){
						Map<String, Object>relInfoMap = (Map)relInfoList.get(r);
						HashMap<String, Object> relParamMap = new HashMap();
						relParamMap.put("modoid", modOID);
						relParamMap.put("reloid", (String)relInfoMap.get("reloid"));
						relParamMap.put("ref", "F");
						String maxVerprtOid = (String)partMngDAO.selectMaxVerPrtOid(relParamMap);
						if(maxVerprtOid != null && !maxVerprtOid.equals("")) relParamMap.put("reloid", maxVerprtOid);
						relInfoArrayList.add(relParamMap);
					}
				}
				relInfoTemp.addAll(relInfoArrayList);
				relInfoArrayList.clear();
				relInfoArrayList.addAll(relInfoTemp);
				if(relInfoArrayList != null && relInfoArrayList.size() > 0){
					for(int a = 0; a < relInfoArrayList.size(); a++){
						HashMap<String, Object>relInfoArrayMap = (HashMap)relInfoArrayList.get(a);
						drawMngDAO.insertDrawRelationInfoRef(relInfoArrayMap);
					}
				}
				
				//5.버전생성되는도면(oldmodoid)에 첨부된 문서를 참조로 신규생성해준다.
				paramMap.put("ref", "F");
				drawMngDAO.insertDrawRelationInfoDocRef(paramMap);
			}
			result.put("modoid", modOID);
			result.put("resultMsg", resultMsg);
			return result;
	    }
	    
	    public void registertEbomInfo(HashMap<String, Object> map) throws Exception{
	    	drawMngDAO.registertEbomInfo(map);
	    }
	    
	    public int updateDrawMasterFile(HashMap<String, Object> map) throws Exception{
	    	return drawMngDAO.updateDrawMasterFile(map);
	    }
	    
		public int updateDrawMasterFileAllCheckDelete(HashMap<String, Object> map) throws Exception{
			return drawMngDAO.updateDrawMasterFileAllCheckDelete(map);
		}
	    /* 
	     * CAG I/G용 프로젝트 스테이지 검색
	     */
		public List<HashMap<String,String>> selectPrjStageByPrjMain(HashMap<String, String> map) throws Exception {
			return prjDAO.selectPrjStageByPrjMain(map);
		}
	    
		/*
		 * 3D도면 복사
		 */
		private boolean copyTo2(String currentFileName, String afterFileName) {
			//스트림, 채널 선언
			FileInputStream inputStream = null;
			FileOutputStream outputStream = null;
			FileChannel fcin = null;
			FileChannel fcout = null;
			
			File afterFile = new File(afterFileName);
				
			if (afterFile.exists())
			{
				afterFile.delete();
			}
				
				
			File currentFile = new File(currentFileName);
			
			if(currentFile.exists()){
				try {
					//스트림 생성
					inputStream = new FileInputStream(currentFile);
					outputStream = new FileOutputStream(afterFile);
					//채널 생성
					fcin = inputStream.getChannel();
					fcout = outputStream.getChannel();
				   
					//채널을 통한 스트림 전송
					long size = fcin.size();
					fcin.transferTo(0, size, fcout);
				} catch (Exception e) {
					System.out.println("Error Occurrence");
					return false;
				} finally {
					//자원 해제
					try {
						fcout.close();
						fcin.close();
						outputStream.close();
						inputStream.close();
					} catch (java.io.IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			return true;
		 }
		
		/*
		 * 현재 문서의 파일에 대한 새로운 문서 버젼 파일을 생성한다.
		 */
		private boolean copyTo(String filePath, String currentFileName, String afterFileName) {
			
			FileInputStream inputStream = null;
			FileOutputStream outputStream = null;
			FileChannel fcin = null;
			FileChannel fcout = null;
			File afterFile = new File(filePath + File.separator + afterFileName);
			
			if (afterFile.exists())
			{
				afterFile.delete();
			}
			
			File currentFile = new File(filePath + File.separator + currentFileName);
			
			try {
				//스트림 생성
				inputStream = new FileInputStream(currentFile);
				outputStream = new FileOutputStream(afterFile);
				//채널 생성
				fcin = inputStream.getChannel();
				fcout = outputStream.getChannel();
			   
				//채널을 통한 스트림 전송
				long size = fcin.size();
				fcin.transferTo(0, size, fcout);
			} catch (Exception e) {
				System.out.println("Error Occurrence");
				return false;
			} finally {
				//자원 해제
				try {
					fcout.close();
					fcin.close();
					outputStream.close();
					inputStream.close();
				} catch (java.io.IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			return true;
		}
		
		/***************************** CAG I/G 용 쿼리 *****************************************************/
		public Object getSql(String module, Map param){
			
			StringBuffer sb = new StringBuffer();
			String lang = param.get("LANG") == null ? "KR" : param.get("LANG").toString();
			lang = ("KR".equals(lang)) ? "name" : lang;
			
			if( "mod".equals(module) ){
				/******************************************** 도면 검색 ************************************************/
					String _module = "mod";
					sb.append("	select m.oid as modoid, car.name as enginename, prttype.name as prttypname, moduletype.name as moduletypename, m.dno, m.mversion, m.eono, modtype.name as modtypename,  \n");
					sb.append(" modsize.name as modsizename, devstep.name as devstepname, m.staoid as msoid, sta.name as ms, h.name as regname, to_char(m.regdate, 'yyyy-mm-dd') regdate, to_char(m.checkdate, 'yyyy-mm-dd hh24:mm:ss') checkdate, CASE WHEN m.checkdate is not null THEN 'T' ELSE 'F' END checkflag \n");
					sb.append("	from mod m 						 	 \n");
					sb.append("	inner join (select max(mversion) mversion, dno from mod group by dno) m2 \n");
					sb.append("	on m.dno = m2.dno and m.mversion = m2.mversion \n");
					sb.append("	left join engctgview car		 	 \n");
					sb.append("	on m.caroid = car.oid  			 	 \n");
					sb.append("	left join ccn prttype			 	 \n");
					sb.append("	on m.prttypeoid = prttype.oid  		 \n");
					sb.append("	left join ccn moduletype 		 	 \n");
					sb.append("	on m.moduletype = moduletype.oid     \n");
					sb.append("	left join ccn modtype    			 \n");
					sb.append("	on m.modtypeoid = modtype.oid   	 \n");
					sb.append("	left join ccn modsize 				 \n");
					sb.append(" on m.modsizeoid = modsize.name 		 \n");
					sb.append("	left join ccn devstep 				 \n");
					sb.append("	on m.devstep = devstep.oid    		 \n");
					sb.append("	inner join ccn sta  				 \n");
					sb.append("	on m.staoid = sta.oid  				 \n");
					sb.append("	inner join hum h  					 \n");
					sb.append("	on m.reghumid = h.id				 \n");	
					sb.append("	where 1=1							 \n");

					/* 차종 */
					if(param.get("engctgoid") != null && !"".equals(param.get("engctgoid"))){
						sb.append(" AND    m.caroid = '"+param.get("engctgoid")+"' \n");
					}

					/* 제품구분 */
					if(param.get("prttypeoid") != null && !"".equals(param.get("prttypeoid"))){
						sb.append(" AND    m.prttypeoid = '"+param.get("prttypeoid")+"' \n");
					}
					
					/* 도면번호 */
					if(param.get("dno") != null && !"".equals(param.get("dno"))){
						sb.append(" AND    UPPER(m.dno) LIKE UPPER( '%"+param.get("dno")+"%' ) \n");
					}
					
					/* 도면개정 */
					if(param.get("mversion") != null && !"".equals(param.get("mversion"))){
						sb.append(" AND   m.mversion = '"+param.get("mversion")+"' \n");
					}
					
					/* 도면구분 */
					if(param.get("moduletype") != null && !"".equals(param.get("moduletype"))){
						sb.append(" AND    m.MODULETYPE = '"+param.get("moduletype")+"' \n");
					}
					
					/* 도면종류 */
					if(param.get("modtypeoid") != null && !"".equals(param.get("modtypeoid"))){
						sb.append(" AND   m.modtypeoid = '"+param.get("modtypeoid")+"'  \n");
					}
					
					/* EONO */
					if(param.get("eono") != null && !"".equals(param.get("eono"))){
						sb.append(" AND    UPPER(m.eono) LIKE UPPER( '%"+param.get("eono")+"%' ) \n");
					}
					
					
					/* 도면크기 */
					if(param.get("modsizeoid") != null && !"".equals(param.get("modsizeoid"))){
						sb.append(" AND   m.modsizeoid = '"+param.get("modsizeoid")+"' \n");
					}
					
					/* 개발단계 */
					if(param.get("devstep") != null && !"".equals(param.get("devstep"))){
						sb.append(" AND   m.devstep = '"+param.get("devstep")+"' \n");
					}

					/* 작성자 */
					if(param.get("username") != null && !"".equals(param.get("username"))){
						sb.append(" AND    h.name = '"+param.get("username")+"' \n");
					}
					
					
					if(param.get("startDate") != null && !"".equals(param.get("startDate"))){
						sb.append(" AND    m.regdate >= '"+param.get("startDate")+"' \n");
					}
					
					if(param.get("endDate") != null && !"".equals(param.get("endDate"))){
						sb.append(" AND    m.regdate <= '"+param.get("endDate")+"' \n");
					}
					
					/* 상태 */
					if(param.get("staoid") != null && !"".equals(param.get("staoid"))){
						sb.append(" AND    m.STAOID = '"+param.get("staoid")+"' \n");
					}
					
			}
			return sb.toString();
		}
		
		public String[] strTokenizer(String str){
			int cnt = 0;
			StringTokenizer st = new StringTokenizer(str,".");
			String[] strToken = new String[st.countTokens()];
			while(st.hasMoreTokens()){
				strToken[cnt] = st.nextToken();
				cnt ++;
			}
			return strToken;
		}

		/* ******************************** 도면 배포 Service Start ******************************************* */
		public List<Map<String, Object>> selectDrawDistTeam() throws Exception {
			return drawMngDAO.selectDrawDistTeam();
		}
		
		public List<Map<String, Object>> selectDistCooperTbl() throws Exception {
			return drawMngDAO.selectDistCooperTbl();
		}

		public List<Map<String, Object>> selectCooperManageList(Map<String, Object> map) throws Exception {
			if(map.get("end") != null && Integer.parseInt((String)map.get("end")) < 0)
				map.put("where", "where");
			return drawMngDAO.selectCooperManageList(map);
		}

		public int selectCooperManageListCnt() throws Exception {
			return drawMngDAO.selectCooperManageListCnt();
		}
		
		public List<Map<String, Object>> selectDrawDistTeamList(Map<String, Object> map) throws Exception {
			if(map.get("end") != null && Integer.parseInt((String)map.get("end")) < 0)
				map.put("where", "where");
			return drawMngDAO.selectDrawDistTeamList(map);
		}

		public int selectDrawDistTeamListCnt() throws Exception {
			return drawMngDAO.selectDrawDistTeamListCnt();
		}
		
		public void insertDistTeamHum(List<Map<String, Object>> list) throws Exception {
			for(Map<String, Object> map: list) {
				String oid = dtrIdGnrService.getNextStringId();
				map.put("oid", oid);
			}
			drawMngDAO.insertDistTeamHum(list);
		}
		
		public void deleteDistTeamHum(String oidArry) throws Exception {
			String [] oid = oidArry.split(";");
			drawMngDAO.deleteDistTeamHum(oid);
		}
		
		@Transactional(propagation = Propagation.REQUIRED,rollbackFor={Exception.class})
		public String insertDrawingRegistrationMain(Map<String, Object> map) throws Exception {
			String distoid = drawDistIdgenService.getNextStringId();
			map.put("distoid", distoid);
			drawMngDAO.insertDrawingRegistrationMain(map);
			return distoid;
		}
		
		public void insertDrawDistCom(Map<String, Object> map) throws Exception {
			drawMngDAO.insertDrawDistCom(map);
		}
		
		public void deleteDrawDistCom(String[] oid) throws Exception {
			drawMngDAO.deleteDrawDistCom(oid);
		}

		@Override
		public List<Map<String, Object>> selectDistSearching(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectDistSearching(map);
		}

		@Override
		public int selectDistSearchingCnt(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectDistSearchingCnt(map);
		}
		
		@Override
		public void updateDrawDistCom(Map<String, Object> map) throws Exception {
			drawMngDAO.updateDrawDistCom(map);
		}

		@Override
		public void insertDistAttachFile(Map<String, Object> map) throws Exception {
			map.put("oid", distfileOidGnrService.getNextStringId());
			drawMngDAO.insertDistAttachFile(map);
		}
		
		@Override
		public void registertDistModHistoryInfo(Map<String, Object> distModMap) throws Exception {
			drawMngDAO.registertDistModHistoryInfo(distModMap);
		}
		
		@Override
		public void registertDistTeamHistoryInfo(Map<String, Object> distModMap) throws Exception {
			drawMngDAO.registertDistTeamHistoryInfo((HashMap)distModMap);
		}

		@Override
		public List<Map<String, Object>> selectDistInsideList(String distoid)
				throws Exception {
			return drawMngDAO.selectDistInsideList(distoid);
		}

		@Override
		public int selectDistInsideListCnt(String distoid) throws Exception {
			return drawMngDAO.selectDistInsideListCnt(distoid);
		}

		@Override
		public List<Map<String, Object>> selectDistDrawFileList(String distoid)
				throws Exception {
			return drawMngDAO.selectDistDrawFileList(distoid);
		}

		@Override
		public int selectDistDrawFileListCnt(String distoid) throws Exception {
			return drawMngDAO.selectDistDrawFileListCnt(distoid);
		}

		@Override
		public List<Map<String, Object>> selectDistDrawList(String distoid)
				throws Exception {
			return drawMngDAO.selectDistDrawList(distoid);
		}

		@Override
		public int selectDistDrawListCnt(String distoid) throws Exception {
			return drawMngDAO.selectDistDrawListCnt(distoid);
		}

		@Override
		public List<Map<String, Object>> selectSearchTeamList(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectSearchTeamList(map);
		}

		@Override
		public int selectSearchTeamListCnt(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectSearchTeamListCnt(map);
		}
		
		public List<Map<String, Object>> selectDistcomhisryList(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectDistcomhisryList(map);
		}

		public int selectDistcomhisryListCnt(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectDistcomhisryListCnt(map);
		}
		
		public void insertDrawDistComHistory(Map<String, Object> map) throws Exception {
			String distoid = (String) map.get("distoid");
			String distcomoidArry[] = ((String) map.get("distcomoid")).split(";");
			
			
			int cnt = drawMngDAO.selectDisthumid(map);
			if(cnt == 0) {
				Exception e = new YuraException();
				throw e;
			}
			for(int i = 0; i < distcomoidArry.length; i++ ) {
				Map<String, Object> paramMap = new HashMap<String, Object>();
				String distcomoid = distcomoidArry[i];
				paramMap.put("distoid", 	distoid);
				paramMap.put("distcomoid", 	distcomoid);
				drawMngDAO.insertDrawDistComHistory(paramMap);
			}
			drawMngDAO.updateDistDraw(distoid);
		}
		
		public void insertModhistory(Map<String, Object> map) throws Exception {
			drawMngDAO.insertModhistory(map);
		}
		
		public void insertDrawFile(Map<String, Object> map) throws Exception {
			drawMngDAO.insertDrawFile(map);
		}
		
		public void updateCADLoginCheck(Map<String, Object> map) throws Exception {
			drawMngDAO.updateCADLoginCheck(map);
		}
		
		public void insertDrawFileList(List<FileVO> listFile, Map<String, Object> map) throws Exception {
			for(FileVO fileVO : listFile) {
				Map<String, Object> paramMap = new HashMap<String, Object>();
				paramMap.put("distoid", 	map.get("distoid"));
				paramMap.put("oid", 		distfileOidGnrService.getNextStringId());
				paramMap.put("humid", 		map.get("humid"));
				paramMap.put("rfilename", 	fileVO.getFileName());
				paramMap.put("filename", 	fileVO.getPhysicalName());
				drawMngDAO.insertDrawFileList(paramMap);
			}
		}
		
		public void deleteDistFile(Map<String, Object> map) throws Exception {
			String fileoidArray[] = ((String)map.get("distfileoid")).split(";"); 
			String distoid = (String) map.get("distoid");
					//	 distoid : $('#distoid').val(),
					//   distfileoid : dataArry[i].distfileoid
			for(int i = 0; i < fileoidArray.length; i++) {
				Map<String, Object> paramMap = new HashMap<String, Object>();
				paramMap.put("distfileoid", fileoidArray[i]	);
				paramMap.put("modoid", fileoidArray[i]	);
				paramMap.put("distoid", 	distoid			);
				drawMngDAO.deleteDistFile(paramMap);
				drawMngDAO.deleteDISTMODHistory(paramMap);
			}
		}

		@Override
		public void deleteCooperManage(String oid, String userid) throws Exception {
			String  arry[] = oid.split(";");
			for(String data : arry) {
				drawMngDAO.deleteCooperManage(data);
				
				//사용이력 등록
				Map<String, Object>vo = new HashMap<String, Object>();
				vo.put("refoid", data);
				vo.put("userid", userid);
				vo.put("usetype", "D");
				userMngService.insertUseHistory(vo);
			}
		}
		
		@Override
		public List<Map<String, Object>> selectCcnUnitList(String parentoid) throws Exception {
			return drawMngDAO.selectCcnUnitList(parentoid);
		}
		
		public List<Map<String, Object>> selectTeamcomList() throws Exception {
			return drawMngDAO.selectTeamcomList();
		}
		
		@Override
		public void updateDistHistory(Map<String, Object> map) throws Exception {
			drawMngDAO.updateDistHistory(map);
		}

		@Override
		public void deleteDistHistory(Map<String, Object> map) throws Exception {
			drawMngDAO.deleteDISTMODHistory(map);
			drawMngDAO.deleteDISTTEAMHistory(map);
			drawMngDAO.deleteDISTFILEHistory(map);
			drawMngDAO.deleteDISTHistory(map);
		}
		
		public List<Map<String, Object>> selectCooperDistSearching(Map<String, Object> map) throws Exception{
			return drawMngDAO.selectCooperDistSearching(map);
		}

		public List<Map<String, Object>> selectComDistComp(Map<String, Object> map) throws Exception{
			return drawMngDAO.selectComDistComp(map);
		}
		
		public List<Map<String, Object>> selectDistTeamEmail(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectDistTeamEmail(map);
		}
		
		public List<Map<String, Object>> selectDistTeamSendEmail(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectDistTeamSendEmail(map);
		}
		
		public List<Map<String, Object>> selectDistFileList(HashMap<String, Object> map) throws Exception {
			return drawMngDAO.selectDistFileList(map);
		}
		
		public List<Map<String, Object>> selectDistDrawComInfo(HashMap<String, Object> map) throws Exception {
			return drawMngDAO.selectDistDrawComInfo(map);
		}
		
		public List<Map<String, Object>> selectDistDrawTeamInfo(HashMap<String, Object> map) throws Exception {
			return drawMngDAO.selectDistDrawTeamInfo(map);
		}
		
		public int updateDrawFileInfo(Map<String, Object> map) throws Exception {
			return drawMngDAO.updateDrawFileInfo(map);
		}
		
		/*
		 * 메일발송:배포도면 중 협력업체 수령지연일 3일 이상인 업체에 메일발송
		 */
		public void sendMailDistDrawDelay(HashMap<String, Object> map) throws Exception {
			try{
				
				List<Map<String, Object>> distComList = drawMngDAO.selectDelayDistComInfo(map);
				for(Map<String, Object> resultMap: distComList){
					String content = "<br> yPLM 배포도면을 확인하시기 바랍니다.";
					content += "<br><br> 협력업체명:"+ resultMap.get("name").toString();
					content += "<br><br> 협력업체 담당자명:"+ resultMap.get("labor").toString();
					content += "<br><br> 담당자 이메일:"+ resultMap.get("email").toString();
					content += "<br><br><br> ※ 본 메일은 수신 전용입니다.";
//					content += "<br><br><a href='"+systemURL+"' target='_blank'>→ yPLM시스템 바로가기</a>";
	
					//메일 발송
					SndngMailVO sndngMailVO = new SndngMailVO();
			    	sndngMailVO.setDsptchPerson("yPlmMaster");
//			    	sndngMailVO.setRecptnPerson("ddang72@yura.co.kr");
			    	sndngMailVO.setRecptnPerson(resultMap.get("email").toString());
			    	sndngMailVO.setSj("[yPLM] 배포도면 접수확인 안내");
			    	sndngMailVO.setEmailCn(content);
			    	sndngMailVO.setAtchFileId(""); 
			    	/*
			    	sndngMailVO.setAtchFileId("AF"); //첨부파일 발송을 위해 값을 AF라 설정함.
			    	String filepath = EgovProperties.getProperty("Globals.fileStorePath.ec")+"ecr_sample.xlsx";
			    	sndngMailVO.setFileStreCours(filepath);
			    	sndngMailVO.setOrignlFileNm("ecTestFile.xlsx");
			    	*/
			    	sndngMailRegistService.insertSndngMailH(null, sndngMailVO);
				}
			}catch(Exception e){
				
			}
		}
		
		/*
		 * 메일발송:배포도면 중 배포팀 수령지연일 3일 이상인 배포접수 담당자에 메일발송
		 */
		public void sendMailDistTeamDrawDelay(HashMap<String, Object> map) throws Exception {
			try{
				
				List<Map<String, Object>> distComList = drawMngDAO.selectDistTeamSendEmail(map);
				for(Map<String, Object> resultMap: distComList){
					String content = "<br> yPLM 배포도면을 확인하시기 바랍니다.";
					content += "<br><br> 담당부서명:"+ resultMap.get("teamname").toString();
					content += "<br><br> 접수 담당자명:"+ resultMap.get("humname").toString();
					content += "<br><br> 담당자 이메일:"+ resultMap.get("email").toString();
					content += "<br><br><br> ※ 본 메일은 수신 전용입니다.";
	
					//메일 발송
					SndngMailVO sndngMailVO = new SndngMailVO();
			    	sndngMailVO.setDsptchPerson("yPlmMaster");
//			    	sndngMailVO.setRecptnPerson("ddang72@yura.co.kr");
			    	sndngMailVO.setRecptnPerson(resultMap.get("email").toString());
			    	sndngMailVO.setSj("[yPLM] 배포도면 접수확인 안내");
			    	sndngMailVO.setEmailCn(content);
			    	sndngMailVO.setAtchFileId(""); 
			    	/*
			    	sndngMailVO.setAtchFileId("AF"); //첨부파일 발송을 위해 값을 AF라 설정함.
			    	String filepath = EgovProperties.getProperty("Globals.fileStorePath.ec")+"ecr_sample.xlsx";
			    	sndngMailVO.setFileStreCours(filepath);
			    	sndngMailVO.setOrignlFileNm("ecTestFile.xlsx");
			    	*/
			    	sndngMailRegistService.insertSndngMailH(null, sndngMailVO);
				}
			}catch(Exception e){
				
			}
		}
		
	    /**사용자 사용 이력 등록
	     * 필수 인자값
	     * refoid : 해당 모듈의 oid값
	     * reftype : 해당 모듈 유형으로 oid의 3자리를 뜻함.
	     *           1) 프로젝트 PRJ  2) 문서 DOC
	     *           3) 파트 PRT       4) 도면 MOD
	     *           5) 설계변경 ECO 6) 결재 WDP
	     * userid : 등록자
	     * usetype : 사용유형으로 1개 문자를 아래 유형분류중 하나 입력해야함
	     *           1) C 등록     2) U 수정
	     *           3) D 삭제     4) R 조회
	     *           5) O 체크아웃 6) I 체크인
	     *           7) L 다운로드
	     * */
	    public void insertModUseHistory(Map<String, Object> vo) throws Exception{
	    	drawMngDAO.insertModUseHistory(vo);
	    }
	    
	    /** 도면배포 접수현황 */
	    public List<Map<String, Object>> selectDistStatus(Map<String, Object> map) throws Exception {
	    	return drawMngDAO.selectDistStatus(map);	
	    }
	    
	    public void updateDistAppFlag(Map<String, Object> map) throws Exception {
	    	drawMngDAO.update("DrawMngDAO.updateDistAppFlag", map);
	    }
	    
		public void updateDrawDistOid(Map<String, Object> map) throws Exception {
			drawMngDAO.update("DrawMngDAO.updateDrawDistOid", map);
		}
		
		/** 도면결재시 배포팀 메일발송을 위한 사용자 메일정보 */
	    public List<Map<String, Object>> selectAppDistTeamMail(Map<String, Object> map) throws Exception {
	    	return drawMngDAO.selectAppDistTeamMail(map);
	    }

	    /** 동일개정정보로 존재하는지 값체크 */
	    public int retrieveCheckExistRev(Map<String,Object> map) throws Exception {
	    	return drawMngDAO.retrieveCheckExistRev(map);
	    }
	    
	    /** 체크아웃/인 여부 확인 */
	    public String selectCheckOutFlag(Map<String,Object> map) throws Exception {
	    	return drawMngDAO.selectCheckOutFlag(map);
	    }
	    
		@Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	    public String selectCheckInOutProcess(Map<String,Object> map, List<FileVO> listFile) throws Exception {
			LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
			String result = "success";
			String bomoid = (String)map.get("bomoid");
			String[] modIds = (String[])map.get("modIds");
			String[] modFileIds = (String[])map.get("modFileIds");
			String[] modFiles = (String[])map.get("modFiles");
			String[] modFileVersions = new String[modFileIds.length];
			String[] modFileNewVersions = new String[modFileIds.length];
			String[] modFilePaths = new String[modFileIds.length];
			String procType = (String)map.get("procType");
	    	SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
			Date today = new Date();
			File targetForder = null;
			String filepath = "";
			String source = ""; 
			String target = "";
			try {
				if(modFileIds != null && modFileIds.length > 0){
					List<Map<String, Object>> tempFileList = new ArrayList();
					for(int j = 0; j < modFileIds.length; j++){
						HashMap<String, Object> modFileparamMap = new HashMap<String, Object>();
						modFileparamMap.put("modfileoid", modFileIds[j]);
						modFileparamMap.put("humid", loginVO.getId());
						if(procType != null && procType.equals("checkout")){
							List<Map<String, Object>> chkFileInfoList = drawMngDAO.selectSubAsmList(modFileparamMap);
							if(chkFileInfoList != null && chkFileInfoList.size() > 0){
								Map<String, Object> chkFileInfoMap = (Map)chkFileInfoList.get(0);
								String modoid = (String)chkFileInfoMap.get("modoid");
								String dno = (String)chkFileInfoMap.get("dno");
								String mversion = (String)chkFileInfoMap.get("mversion");
								String rfilename = (String)chkFileInfoMap.get("rfilename");
								String mfilename = (String)chkFileInfoMap.get("filename");
								String masterflag = (String)chkFileInfoMap.get("mk");
								long checkOutTime = today.getTime();
								modFileparamMap.put("checkdate", sf.format(checkOutTime));
								if(masterflag != null && masterflag.equals("T")){
									modFileparamMap.put("modoid",modoid);
									drawMngDAO.updateCheckOut(modFileparamMap);   
								}
								
								if(modoid != null && modoid.equals(bomoid)){
									filepath = dno+"-"+mversion+".zip"; 
									source = dno+"-"+mversion+".zip"; 
									target = dno+"-"+mversion+".zip"; 
								}
								modFileparamMap.put("rfilename", rfilename);
								modFileparamMap.put("mfilename", mfilename);
								modFileparamMap.put("lastmoddate",checkOutTime);          
								tempFileList.add(modFileparamMap);
								drawMngDAO.updateModFileCheckOut(modFileparamMap);    
							}
						}else if(procType != null && procType.equals("checkin")){
							List<Map<String, Object>> chkFileInfoList = drawMngDAO.selectSubAsmList(modFileparamMap);
							if(chkFileInfoList != null && chkFileInfoList.size() > 0){
								Map<String, Object> chkFileInfoMap = (Map)chkFileInfoList.get(0);
								String oid = (String)chkFileInfoMap.get("oid");
								String version = (String)chkFileInfoMap.get("version");
								modFileVersions[j] = version;
								String newVersion = formatNumber2((Integer.parseInt(version) + 1));
								modFileNewVersions[j] = newVersion;
								String modoid = (String)chkFileInfoMap.get("modoid");
								String dno = (String)chkFileInfoMap.get("dno");
								String mversion = (String)chkFileInfoMap.get("mversion");
								String rfilename = (String)chkFileInfoMap.get("rfilename");
								String mfilename = (String)chkFileInfoMap.get("filename");
								String masterflag = (String)chkFileInfoMap.get("mk");
								modFilePaths[j] = (String)chkFileInfoMap.get("filepath");
								if(masterflag != null && masterflag.equals("T")){
									modFileparamMap.put("modoid",modoid);
									drawMngDAO.updateCheckIn(modFileparamMap);    
								}
								modFileparamMap.put("rfilename", rfilename);
								modFileparamMap.put("mfilename", mfilename);   
							}
						}
					}
					
					if(procType != null && procType.equals("checkin")){
						int fileCnt = 0;
						for(FileVO fileVO : listFile) {
							Map<String, Object> modFileMap = new HashMap<String, Object>();
							modFileMap.put("oid", modFileIds[fileCnt]);
							modFileMap.put("version", modFileVersions[fileCnt]);  
							modFileMap.put("newversion", modFileNewVersions[fileCnt]);
							modFileMap.put("filesize", fileVO.getSize());
							modFileMap.put("filename", fileVO.getPhysicalName());
							modFileMap.put("rfilename", fileVO.getFileName());
							modFileMap.put("filepath", modFilePaths[fileCnt]);
							String realpath = (String)modFilePaths[fileCnt];
							if(realpath != null && !realpath.equals("")){
								realpath = uploadDir + realpath + File.separator + fileVO.getPhysicalName();
							}else{
								realpath = uploadDir + File.separator + fileVO.getPhysicalName();
							}
							drawMngDAO.insertModFileHistory(modFileMap);
							drawMngDAO.updateModFileCheckIn(modFileMap);  
							copyTo2(uploadDirTemp + File.separator + fileVO.getPhysicalName(), realpath);
							File modFile = new File(uploadDirTemp + File.separator + fileVO.getPhysicalName());
							if(modFile.exists()){
								modFile.delete();
							}
							fileCnt++;
						}
					}
					
					/*
					if(procType != null && procType.equals("checkout")){
						if(tempFileList != null){
							makeZipFile2(tempFileList, uploadDirTemp, filepath);
							fileDownload(request, source, target, response);
						}
					}
					if(tempFileList != null){
						for(int j = 0; j < tempFileList.size(); j++){
							Map tempFileMap = (Map)tempFileList.get(j);
							String rfilename = (String)tempFileMap.get("rfilename");
							File tempFile = new File(uploadDirTemp + rfilename);
							if(tempFile.isFile()){
								tempFile.delete();
							}
						}
					}*/
					
				}
			} catch (Exception e) {
				result = "fali";
				System.out.println("체크인/아웃 에러 발생");
			}finally{
				
			}
			
			return result;
	    }
	    
	    /** 도면배포 파일 다운로드 이력조회 */
	    public List<Map<String, Object>> selecDistDownHist(Map<String, Object> map) throws Exception{
	    	return drawMngDAO.selecDistDownHist(map);
	    }

		public List<Map<String, Object>> selectRelExist(HashMap<String, Object> paramMap) throws Exception{
			return drawMngDAO.selectRelExist(paramMap);
		}
	   
		public List<Map<String, Object>> retrieveMasterFileInfo(Map<String, Object> map) throws Exception{
			return drawMngDAO.retrieveMasterFileInfo(map);
		}
		
		public void updateModuleCheckMigration(Map<String, Object> map) throws Exception{
			try{
				String path="D://util//yPLM//upload//document//05.Document//";
				subDirList(path);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		public void subDirList(String source)throws IOException{
			String path="D://util//yPLM//upload//document//";
			try{
				File dir = new File(source); 
				File[] fileList = dir.listFiles(); 
				for(int i = 0 ; i < fileList.length ; i++){
					Map<String, Object> paramMap = new HashMap<String, Object>();
					File file = fileList[i]; 
					if(file.isFile()){
						System.out.println("\t 파일 이름 = " + file.getName());
						paramMap.put("filename", file.getName());
						List<Map<String, Object>> fileInfo = drawMngDAO.selectModuleFileInfo(paramMap);
						if(fileInfo != null && fileInfo.size() > 0){
							Map fileInfoMap = (Map)fileInfo.get(0);
							String regdate = (String)fileInfoMap.get("regdate");
							String filename = (String)fileInfoMap.get("mfilename");
							path = path+regdate;
							File Folder = new File(path);
							if (!Folder.exists()) {
								try{
								    Folder.mkdir(); 
								    System.out.println("폴더가 생성되었습니다.");
							    } 
							    catch(Exception e){
								    e.getStackTrace();
							    }
							}
							path = path+File.separator+filename;
							File outFile = new File(path);
							migFileCopy(file, outFile);
							paramMap.put("filepath", regdate+File.separator);
							drawMngDAO.updateModuleFileInfo(paramMap);
						}
					}else{
						System.out.println("디렉토리 이름 = " + file.getName());
						subDirList(file.getCanonicalPath().toString()); 
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		public static void migFileCopy(File inFile, File outFile) throws FileNotFoundException {
			  FileInputStream fis = null;
			  FileOutputStream fos = null;
			  try {
				  fis = new FileInputStream(inFile);
				  fos = new FileOutputStream(outFile);
				  int data = 0;
				   while((data=fis.read())!=-1) {
					   fos.write(data);
				   }
				   fis.close();
				   fos.close();
				   
			   }catch (Exception e) {
				  e.printStackTrace();
			   }
		}
		 
	    /** EBOM정보*/
	    public List<Map<String, Object>> selectEbomTreeList(HashMap<String,Object> map) throws Exception{
	    	List<Map<String, Object>> result = null;
	    	try{
	    		result = drawMngDAO.selectEbomTreeList(map);
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}
	    	return result;
	    }
	    
	    public List<Map<String, Object>> selectRecEbomTreeList(HashMap<String,Object> map) throws Exception{
	    	List<Map<String, Object>> result = null;
	    	try{
	    		result = drawMngDAO.selectRecEbomTreeList(map);
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}
	    	return result;
	    }
	    
		public void call_UpdateEbom(HashMap<String, Object> map) throws Exception{
			drawMngDAO.call_UpdateEbom(map);
		}
		
		public void call_UpdateEbomSeq(HashMap<String, Object> map) throws Exception{
			drawMngDAO.call_UpdateEbomSeq(map);
		}
		
	    public List<Map<String, Object>> selectEbomNotTopPartTreeList(HashMap<String,Object> map) throws Exception{
	    	return drawMngDAO.selectEbomNotTopPartTreeList(map);
		}
		
		public List<Map<String, Object>> selectRootOidList(HashMap<String,Object> map) throws Exception{
			return drawMngDAO.selectRootOidList(map);
		}

	    /** EBOM 파트 등록*/
		public void InsertEbomInfo(HashMap<String, Object> map) throws Exception{
			drawMngDAO.InsertEbomInfo(map);
		}
		/** EBOM 파트 수정*/
		public void UpdateEbomInfo(HashMap<String, Object> map) throws Exception{
			drawMngDAO.UpdateEbomInfo(map);
		}
		/** EBOM 파트 삭제*/
		public int deleteEbomInfo(HashMap<String, Object> map) throws Exception{
			int result = 0;
			try{
				result = drawMngDAO.deleteEbomInfo(map);
				/*
				List<Map<String, Object>> ebomInfo = drawMngDAO.selectEbomInfo(map);
				if(ebomInfo != null && result == 1){
					for(int i = 0; i < ebomInfo.size(); i++){
						Map ebomMap = (Map)ebomInfo.get(i);
						ebomMap.put("seq", i+1);
						drawMngDAO.updateEbomSeq(ebomMap);
					}
				}*/
			}catch(Exception e){
				e.printStackTrace();
			}
			return result;
		}
		
		public void insertDrawFileInfo(HashMap<String, Object> map) throws Exception{
			drawMngDAO.insertDrawFileInfo(map);
	    }
		
		public void deleteDrawFilesInfo(HashMap<String, Object> map) throws Exception{
	    	drawMngDAO.deleteDrawFilesInfo(map);
	    }
		
		public void updateCheckInCADBomUpdate(Map<String, Object> map) throws Exception{
			drawMngDAO.updateCheckInCADBomUpdate(map);
		}
		
		public int updateVerprtRelMod(HashMap<String, Object> map) throws Exception{
			return drawMngDAO.updateVerprtRelMod(map);
		}
		
		public int deleteVerprtRelMod(HashMap<String, Object> map) throws Exception{
			return drawMngDAO.deleteVerprtRelMod(map);
		}
		
		public List<Map<String, Object>> retrieveEbomDrawVerChkList(HashMap<String, Object> map) throws Exception{
			return drawMngDAO.retrieveEbomDrawVerChkList(map);
		}

		public List<Map<String, Object>> selectRelEbomInfo(HashMap<String, Object> map) throws Exception{
			return drawMngDAO.selectRelEbomInfo(map);
		}

		public List<Map<String, Object>> selectSumEbomTreeList(HashMap<String,Object> map) throws Exception{
			return drawMngDAO.selectSumEbomTreeList(map);
		}
	    /**
	     * 도면리스트
	     */
	    public List<Map<String, Object>> retrieveDrawList(HashMap<String, Object> map) throws Exception{
	    	return drawMngDAO.retrieveDrawList(map);
	    }
	    
	    
	    public List<Map<String, Object>> selectDistReceiveTeamList(Map<String, Object> commandMap) throws Exception {
			return drawMngDAO.selectDistReceiveTeamList(commandMap);
		}
		
		public List<Map<String, Object>> retrieveDocInfo(HashMap<String, Object> map) throws Exception {
			return drawMngDAO.retrieveDocInfo(map);
		}
		
		public void insertVerdochistory(Map<String, Object> map) throws Exception {
			drawMngDAO.insertVerdochistory(map);
		}
		
		public List<Map<String, Object>> selectDistDocList(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectDistDocList(map);
		}
		
		public void updateDrawCheckUnlock(Map<String, Object> map) throws Exception {
			drawMngDAO.updateDrawCheckUnlock(map);
		}

		@Transactional(propagation = Propagation.REQUIRED,rollbackFor={Exception.class})
		public void updateBomTreeRootoid(Map<String, Object> map) throws Exception{
			try{
				List<Map<String, Object>> rootList = drawMngDAO.selectBomRootList(map);
				if(rootList != null && rootList.size() > 0){
					for(int i = 0; i < rootList.size(); i++){
						Map rootMap = (Map)rootList.get(i);
						String gbn = (String)rootMap.get("gbn");
						String oid = (String)rootMap.get("oid");
						System.out.println(i);
						if(gbn != null && gbn.equals("prt")){
							System.out.println("----------------------------MBOM START---------------------------------");
							HashMap<String, String> paramMap = new HashMap<String, String>();
							paramMap.put("verprtoid",oid);
							List<Map<String, Object>> mbomTreeList = partMngDAO.selectMbomTreeList(paramMap);
							if(mbomTreeList != null && mbomTreeList.size() > 1){
								for(int m = 0; m < mbomTreeList.size(); m++){
									Map mbomTreeMap = (Map)mbomTreeList.get(m);
									HashMap<String, Object> mbomParamMap = new HashMap<String, Object>();
									String verprtoid = (String)mbomTreeMap.get("verprtoid");
									String parentoid = (String)mbomTreeMap.get("parentoid");
									mbomParamMap.put("gbn", gbn);
									mbomParamMap.put("rootoid", oid);
									mbomParamMap.put("verprtoid", verprtoid);
									mbomParamMap.put("parentoid", parentoid);
									drawMngDAO.updateBomTreeRootoid(mbomParamMap);
								}
							}
							System.out.println("----------------------------MBOM END---------------------------------");
						}else if(gbn != null && gbn.equals("mod")){
							System.out.println("----------------------------EBOM START---------------------------------");
							HashMap<String, Object> paramMap = new HashMap<String, Object>();
							paramMap.put("modoid",oid);
							List<Map<String, Object>> ebomTreeList = drawMngDAO.selectEbomTreeList(paramMap);
							if(ebomTreeList != null && ebomTreeList.size() > 1){
								for(int e = 0; e < ebomTreeList.size(); e++){
									Map ebomTreeMap = (Map)ebomTreeList.get(e);
									HashMap<String, Object> ebomParamMap = new HashMap<String, Object>();
									String modoid = (String)ebomTreeMap.get("modoid");
									String parentoid = (String)ebomTreeMap.get("parentoid");
									ebomParamMap.put("gbn", gbn);
									ebomParamMap.put("rootoid", oid);
									ebomParamMap.put("modoid", modoid);
									ebomParamMap.put("parentoid", parentoid);
									drawMngDAO.updateBomTreeRootoid(ebomParamMap);
								}
							}
							System.out.println("----------------------------EBOM END---------------------------------");
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		public void updateDrawCheckLock(Map<String, Object> map) throws Exception{
			drawMngDAO.updateDrawCheckLock(map);
		}
		
		public void updateDrawFileCheckUnlock(Map<String, Object> map) throws Exception{
			drawMngDAO.updateDrawFileCheckUnlock(map);
		}

		public void updateDrawStatus(Map<String, Object> map) throws Exception{
			drawMngDAO.updateDrawStatus(map);
		}
		
		private String formatNumber2(int num){
			String str = String.format("%02d", num);
			return str;
		}
		
		public void insertEBOMTree(List<Map<String, Object>> map) throws Exception {
			drawMngDAO.insertEBOMTree(map);
		}
		
		public void insertThumbnail(List<Map<String, Object>> map) throws Exception {
			for ( int i = 0; i < map.size(); i++ ) {
				String fildOid = drawFileIdgenService.getNextStringId();
				map.get(i).put("oid", fildOid);
			}
			
			drawMngDAO.insertThumbnail(map);
		}
		
		public void insertAddEBOMTree(List<Map<String, Object>> map) throws Exception {
			drawMngDAO.insertAddEBOMTree(map);
		}
		
		/** CAD I/G 관련 쿼리 */
		public List<Map<String, Object>> selectSearchDrawThumb(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectSearchDrawThumb(map);
		}
		
		public List<Map<String, Object>> selectSearchDraw(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectSearchDraw(map);
		}
		
		public List<Map<String, Object>> selectSearchDraw2(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectSearchDraw2(map);
		}
		
		public List<Map<String, Object>> selectCar(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectCar(map);
		}
		
		public List<Map<String, Object>> selectCancledraw(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectCancledraw(map);
		}
		
		public List<Map<String, Object>> selectMainSearchParent(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectMainSearchParent(map);
		}
		
		public List<Map<String, Object>> selectMainSearchChild(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectMainSearchChild(map);
		}
		
		public List<Map<String, Object>> selectEBOMTreeChild(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectEBOMTreeChild(map);
		}
		
		public List<Map<String, Object>> selectMainSearchModfiles(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectMainSearchModfiles(map);
		}
		
		public List<Map<String, Object>> selectMainSearchModfilehistory(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectMainSearchModfilehistory(map);
		}
		
		public void updateModCheckOut(Map<String, Object> map) throws Exception {
			drawMngDAO.updateModCheckOut(map);
		}
		
		public void updateModCheckOut2(Map<String, Object> map) throws Exception {
			drawMngDAO.updateModCheckOut2(map);
		}
		
		public void updateModfilesCheckOut(Map<String, Object> map) throws Exception {
			drawMngDAO.updateModfilesCheckOut(map);
		}
		
//		public void updateModCancleCheckOut(Map<String, Object> map) throws Exception {
//			drawMngDAO.updateModCancleCheckOut(map);
//		}
//		
//		public void updateModfilesCancleCheckOut(Map<String, Object> map) throws Exception {
//			drawMngDAO.updateModfilesCancleCheckOut(map);
//		}
		
		public void insertModfilehistory2(Map<String, Object> map) throws Exception {
			drawMngDAO.insertModfilehistory2(map);
		}
		
		public void updateCancleDraw(Map<String, Object> map) throws Exception {
			drawMngDAO.updateCancleDraw(map);
		}
		
		public List<Map<String, Object>> selectHumCheck(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectHumCheck(map);
		}
		
		public List<Map<String, Object>> selectModCheckIn(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectModCheckIn(map);
		}
		
		public List<Map<String, Object>> selectModCheckIn2(Map<String, Object> map) throws Exception {
			List<Map<String, Object>> CheckInFiles = null;
			try{				
				String chkoutPath = (String)map.get("chkout_path");
				String chkRootOid = drawMngDAO.selectCheckInRootOid(chkoutPath);
				
				// ebom 구조
				HashMap<String,Object> modInfo = new HashMap<String,Object>();
				modInfo.put("modoid", chkRootOid);
				List<Map<String, Object>> ebomInfo = drawMngDAO.selectEbomTreeList(modInfo);
				
				// ebom 의 modoid로 modfiles 정보 가져오기
				List<String> oidList = new ArrayList<String>();
				for (int i=0; i<ebomInfo.size(); i++){
					Map tempMap = (Map)ebomInfo.get(i);
					oidList.add((String)tempMap.get("modoid"));
				}
				CheckInFiles = drawMngDAO.selectMainSearchModfiles2(oidList);
			}catch(Exception e){
				e.printStackTrace();
			}
			return CheckInFiles;
		}
		
		public List<Map<String, Object>> selectEbomCheckIn(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectEbomCheckIn(map);
		}
		
		public List<Map<String, Object>> selectModfilesCheckIn(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectModfilesCheckIn(map);
		}
		
		public void updateNoAddfileCheckIn(Map<String, Object> map) throws Exception {
			String datas = String.valueOf(map.get("datas"));
			System.out.println("datas:"+datas);
			String[] arrDatas = datas.split("!");
			//List<Map<String,Object>> updateModfiles = new ArrayList<Map<String,Object>>();

			for(int i=0; i<arrDatas.length; i++){
				String[] tempData = arrDatas[i].split("\\|");
				Map<String, Object> tempMap = new HashMap<String, Object>();
				tempMap.put("version", tempData[0]);
				tempMap.put("filepath", tempData[1]);
				tempMap.put("rfilename", tempData[2]);
				tempMap.put("filename", tempData[3]);
				tempMap.put("filesize", Integer.parseInt(tempData[4]));
				tempMap.put("chkoutpath", tempData[5]);
				tempMap.put("oid", tempData[6]);
				
				System.out.println(tempData[0]);
				System.out.println(tempData[1]);
				System.out.println(tempData[2]);
				System.out.println(tempData[3]);
				System.out.println(Integer.parseInt(tempData[4]));
				System.out.println(tempData[5]);
				System.out.println(tempData[6]);
				
				drawMngDAO.updateNoAddfileCheckIn(tempMap);
				
				//updateModfiles.add(tempMap);
			}
			
//			if(updateModfiles.size()>0){
//				drawMngDAO.updateNoAddfileCheckIn(updateModfiles);
//			}
		}
		
		public void updateNoAddfileCheckIn2(Map<String, Object> map) throws Exception {
			drawMngDAO.updateNoAddfileCheckIn2(map);
		}
		
		public List<Map<String, Object>> selectAutoExistInfo(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectAutoExistInfo(map);
		}
		
		public List<Map<String, Object>> selectRegCatchError(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectRegCatchError(map);
		}
		
		public List<Map<String, Object>> selectRegCatchError2(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectRegCatchError2(map);
		}
		
		public List<Map<String, Object>> selectComtecopseq(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectComtecopseq(map);
		}
		
		public void insertAddDataMod(Map<String, Object> map) throws Exception {
			try{
				String datas = String.valueOf(map.get("add_mod_datas"));
				System.out.println("add_mod_datas:"+datas);
				String[] arrDatas = datas.split("!");
				List<Map<String,Object>> insertMod = new ArrayList<Map<String,Object>>();
	
				for(int i=0; i<arrDatas.length; i++){
					String[] tempData = arrDatas[i].split("\\|");
					Map<String, Object> tempMap = new HashMap<String, Object>();
					tempMap.put("oid", tempData[0]);
					tempMap.put("caroid", tempData[1]);
					tempMap.put("prttypeoid", tempData[2]);
					tempMap.put("dno", tempData[3]);
					tempMap.put("mversion", tempData[4]);
					tempMap.put("moduletype", tempData[5]);
					tempMap.put("modtypeoid", tempData[6]);
					tempMap.put("eono", tempData[7]);
					tempMap.put("dscoid", tempData[8]);
					tempMap.put("modsizeoid", tempData[9]);
					tempMap.put("devstep", tempData[10]);
					tempMap.put("get_radio", tempData[11]);
					tempMap.put("regid", tempData[12]);
					tempMap.put("nowDate", tempData[13]);
					tempMap.put("dnam", tempData[14]);
					
					insertMod.add(tempMap);
				}
				
				if(insertMod.size()>0){
					drawMngDAO.insertAddDataMod(insertMod);
				}
			}catch(Exception e){
				System.out.println("왜에러나냐??????????");
				e.printStackTrace();
			}
		}
		
		public void updateComtecopseq(Map<String, Object> map) throws Exception {
			drawMngDAO.updateComtecopseq(map);
		}
		
		public void insertDrawrel(Map<String, Object> map) throws Exception {
			String datas = String.valueOf(map.get("add_drawRel_datas"));
			System.out.println("add_drawRel_datas:"+datas);
			String[] arrDatas = datas.split("!");
			List<Map<String,Object>> insertdrawrel = new ArrayList<Map<String,Object>>();

			for(int i=0; i<arrDatas.length; i++){
				String[] tempData = arrDatas[i].split("\\|");
				Map<String, Object> tempMap = new HashMap<String, Object>();
				tempMap.put("oid", tempData[0]);
				tempMap.put("reloid", tempData[1]);
				tempMap.put("ref", tempData[2]);
				
				insertdrawrel.add(tempMap);
			}
			
			if(insertdrawrel.size()>0){
				drawMngDAO.insertDrawrel(insertdrawrel);
			}
		}
		
		public void insertAddDataModfiles(Map<String, Object> map) throws Exception {
			String datas = String.valueOf(map.get("add_modfiles_datas"));
			System.out.println("add_modfiles_datas:"+datas);
			String[] arrDatas = datas.split("!");
			List<Map<String,Object>> insertModfiles = new ArrayList<Map<String,Object>>();

			for(int i=0; i<arrDatas.length; i++){
				String[] tempData = arrDatas[i].split("\\|");
				Map<String, Object> tempMap = new HashMap<String, Object>();
				
				tempMap.put("oid", tempData[0]);
				tempMap.put("modoid", tempData[1]);
				tempMap.put("version", tempData[2]);
				tempMap.put("filename", tempData[3]);
				tempMap.put("rfilename", tempData[4]);
				tempMap.put("filesize", Integer.parseInt(tempData[5]));
				tempMap.put("masterflag", tempData[6]);
				tempMap.put("regdate", tempData[7]);
				tempMap.put("humid", tempData[8]);
				tempMap.put("filepath", tempData[9]);
				tempMap.put("indexno", Integer.parseInt(tempData[10]));
				
				insertModfiles.add(tempMap);
			}
			
			if(insertModfiles.size()>0){
				drawMngDAO.insertAddDataModfiles(insertModfiles);
			}
		}
		
		public void insertGetEbomData(Map<String, Object> map) throws Exception {
			String datas = String.valueOf(map.get("datas"));
			System.out.println("datas:"+datas);
			String[] arrDatas = datas.split("!");
			List<Map<String,Object>> insertEbom = new ArrayList<Map<String,Object>>();
			List<Map<String, Object>> resEBOM = null;
			
			for(int i=0; i<arrDatas.length; i++){
				String[] tempData = arrDatas[i].split("\\|");
				Map<String, Object> tempMap = new HashMap<String, Object>();
				tempMap.put("cur", tempData[0]);
				tempMap.put("par", tempData[1]);
				tempMap.put("humid", tempData[2]);
				tempMap.put("seq", Integer.parseInt(tempData[3]));
				
				insertEbom.add(tempMap);
			}
			
			// 중복 제거
			resEBOM = new ArrayList<Map<String, Object>>(new HashSet<Map<String, Object>>(insertEbom));
			
			if(resEBOM.size()>0){
				drawMngDAO.insertGetEbomData(resEBOM);
			}
			
			/*if(insertEbom.size()>0){
				drawMngDAO.insertGetEbomData(insertEbom);
			}*/
		}
		
		public void insertModfilesThumbNail(Map<String, Object> map) throws Exception {
			String datas = String.valueOf(map.get("datas"));
			System.out.println("datas:"+datas);
			String[] arrDatas = datas.split("!");
			List<Map<String,Object>> insertModfilesThumb = new ArrayList<Map<String,Object>>();

			for(int i=0; i<arrDatas.length; i++){
				String[] tempData = arrDatas[i].split("\\|");
				Map<String, Object> tempMap = new HashMap<String, Object>();
				tempMap.put("oid", tempData[0]);
				tempMap.put("version", tempData[1]);
				tempMap.put("modoid", tempData[2]);
				tempMap.put("filepath", tempData[3]);
				tempMap.put("rfilename", tempData[4]);
				tempMap.put("filename", tempData[5]);
				tempMap.put("filesize", Integer.parseInt(tempData[6]));
				tempMap.put("humid", tempData[7]);
				tempMap.put("masterflag", tempData[8]);
				tempMap.put("regdate", tempData[9]);
				tempMap.put("indexno", Integer.parseInt(tempData[10]));
				
				insertModfilesThumb.add(tempMap);
			}
			
			if(insertModfilesThumb.size()>0){
				drawMngDAO.insertModfilesThumbNail(insertModfilesThumb);
			}
		}
		
		public void updateAddDataModfiles(Map<String, Object> map) throws Exception {
			String datas = String.valueOf(map.get("datas"));
			System.out.println("datas:"+datas);
			String[] arrDatas = datas.split("!");
			//List<Map<String,Object>> updateModfiles = new ArrayList<Map<String,Object>>();

			for(int i=0; i<arrDatas.length; i++){
				String[] tempData = arrDatas[i].split("\\|");
				Map<String, Object> tempMap = new HashMap<String, Object>();
				tempMap.put("version", tempData[0]);
				tempMap.put("filepath", tempData[1]);
				tempMap.put("rfilename", tempData[2]);
				tempMap.put("filename", tempData[3]);
				tempMap.put("filesize", Integer.parseInt(tempData[4]));
				tempMap.put("chkoutpath", tempData[5]);
				tempMap.put("oid", tempData[6]);
				
				drawMngDAO.updateAddDataModfiles(tempMap);
				
				//updateModfiles.add(tempMap);
			}
			
//			if(updateModfiles.size()>0){
//				drawMngDAO.updateAddDataModfiles(updateModfiles);
//			}
		}
		
		public List<Map<String, Object>> selectChkrootoid(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectChkrootoid(map);
		}
		
		public List<Map<String, Object>> selectChkOutEbomfiles(HashMap<String, Object> map) throws Exception {
			List<String> modoidList = new ArrayList<String>();
			
			try{
				List<Map<String, Object>> getEbomModoid = drawMngDAO.selectEbomTreeList(map);
		   		if(getEbomModoid != null){
		   			for(int i=0; i < getEbomModoid.size(); i++){
		   				Map ebomMap = (Map)getEbomModoid.get(i);
		   				modoidList.add((String)ebomMap.get("modoid"));
		   			}
		   		}
			}catch(Exception e){
				e.printStackTrace();
	    	}
			
			return drawMngDAO.selectChkOutEbomfiles(modoidList);
		}
		
		public List<Map<String, Object>> selectEbomFileList(HashMap<String, Object> map) throws Exception {
			List<String> modoidList = new ArrayList<String>();	
			List<Map<String, Object>> delEbomList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> oriEbomFileList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> resEbom = null;
			
			try{
				List<Map<String, Object>> getEbomModoid = drawMngDAO.selectEbomTreeList(map);
		   		if(getEbomModoid != null){
		   			for(int i=0; i < getEbomModoid.size(); i++){
		   				Map ebomMap = (Map)getEbomModoid.get(i);
		   				modoidList.add((String)ebomMap.get("modoid"));
		   				
		   				String parentoid = (String)ebomMap.get("parentoid");
		   				if(parentoid != null){
			   				Map<String, Object> putMap = new HashMap<String, Object>();
							putMap.put("modoid", (String)ebomMap.get("modoid"));
							putMap.put("parentoid", parentoid);
							putMap.put("seq", String.valueOf(ebomMap.get("seq")));
			   				delEbomList.add(putMap);
		   				}
		   			}
		   		}		   		
		   		oriEbomFileList = drawMngDAO.selectEbomFileList(modoidList);
		   		
		   		// 기존 ebom 삭제
		   		// drawMngDAO.deleteEbomList(modoidList);
		   		resEbom = new ArrayList<Map<String, Object>>(new HashSet<Map<String, Object>>(delEbomList));
//		   		if(resEbom.size() > 0){
//		   			drawMngDAO.deleteEbomList(resEbom);
//		   		}
		   		for(int k=0; k < resEbom.size(); k++){
		   			Map<String, Object> tempMap = new HashMap<String, Object>();
		   			tempMap = (Map)resEbom.get(k);
		   			drawMngDAO.deleteEbomList(tempMap);
		   		}
		   		
			}catch(Exception e){
				e.printStackTrace();
	    	}
			return oriEbomFileList;
		}

		// 원본
//		public List<Map<String, Object>> selectEbomFileList(HashMap<String, Object> map) throws Exception {
//			List<String> modoidList = new ArrayList<String>();
//			List<Map<String, Object>> oriEbomFileList = new ArrayList<Map<String, Object>>();
//			
//			try{
//				List<Map<String, Object>> getEbomModoid = drawMngDAO.selectEbomTreeList(map);
//		   		if(getEbomModoid != null){
//		   			for(int i=0; i < getEbomModoid.size(); i++){
//		   				Map ebomMap = (Map)getEbomModoid.get(i);
//		   				modoidList.add((String)ebomMap.get("modoid"));
//		   			}
//		   		}		   		
//		   		oriEbomFileList = drawMngDAO.selectEbomFileList(modoidList);
//		   		
//		   		// 기존 ebom 삭제
//		   		drawMngDAO.deleteEbomList(modoidList);
//		   		
//			}catch(Exception e){
//				e.printStackTrace();
//	    	}
//			return oriEbomFileList;
//		}
		
		public List<Map<String, Object>> selectCCN(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectCCN(map);
		}
		
		public void insertNoAddNewEbom(List<Map<String, Object>> map) throws Exception {
			drawMngDAO.insertNoAddNewEbom(map);
		}			
		
		public String FileRenameHash(String tmpFileName, String type){
			String hashName = "";
			try{
				SimpleDateFormat format = new SimpleDateFormat ("yyyyMMddHHmmss");
				String format_time = format.format (System.currentTimeMillis());
				String hash_tmp = tmpFileName + format_time;
				StringBuilder builder = new StringBuilder();
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				md.update(hash_tmp.getBytes());
				for (byte b: md.digest()) {
			          builder.append(String.format("%02x", b));
			    }
				hashName = builder.toString() + type;
			}catch(Exception e){
				e.printStackTrace();
			}
			return hashName;
		}
		
		public long copyFile(String oldPath, String oldFile, String newPath, String newFile){
			String oriFilePath = oldPath + File.separator + oldFile;
			String newFilePath = newPath + File.separator + newFile;
			File oFile = new File(oriFilePath);
			File nFile = new File(newFilePath);
			long fsize = oFile.length();
			if (oFile.exists()) {
				oFile.renameTo(nFile);
			} else {
				System.out.println("파일이 없음..");
			}
			return fsize;
		}
		
		/* 신규등록 - 파트만 */
		public String insertOnlyPartData(Map<String, Object> map) throws Exception{
			String msg = "fail insert data";
			try{
				String humid = (String)map.get("id");
				String moddata = (String)map.get("moddata");
				String thumb_dir = (String)map.get("thumb_dir");
				String drawrel = "";
				String[] arrType = {".jpg", ".CATDrawing", ".pdf", ".wrl"};
				String[] arrDatas = moddata.split("\\|");
				List<Map<String,Object>> tmpModData = new ArrayList<Map<String,Object>>();
				for(int i=0; i<arrDatas.length; i++){
					String[] tempData = arrDatas[i].split(";");
					Map<String, Object> tempMap = new HashMap<String, Object>();
					tempMap.put("filename", tempData[0]);
					tempMap.put("caroid", tempData[1]);
					tempMap.put("prttypeoid", tempData[2]);
					tempMap.put("dno", tempData[3]);
					tempMap.put("mversion", tempData[4]);
					tempMap.put("moduletype", tempData[5]);
					tempMap.put("modtypeoid", tempData[6]);
					tempMap.put("eono", tempData[7]);
					tempMap.put("dscoid", tempData[8]);
					tempMap.put("modsizeoid", tempData[9]);
					tempMap.put("devstep", tempData[10]);
					tempMap.put("verprt", tempData[11]);
					tempMap.put("dnam", tempData[12]);
					tmpModData.add(tempMap);
				}
				
				// 년도 폴더 path 뽑기
				SimpleDateFormat format = new SimpleDateFormat ("yyyyMMddHHmmss");
				String format_time = format.format (System.currentTimeMillis());
				String path = File.separator + format_time.substring(0, 4);
				
				// 파일 이동 및 DB insert (MOD, MODFILES)
				for (int k=0; k<tmpModData.size(); k++){
					Map<String, Object> insertMod = new HashMap<String, Object>();
					Map<String,Object> insertModFiles = new HashMap<String,Object>();
					
					Map<String, Object> getMap = (Map)tmpModData.get(k);
					String fname1 = (String)getMap.get("filename");
					String front_fname = fname1.substring(0, fname1.lastIndexOf("."));
					String type = fname1.substring(fname1.lastIndexOf("."));
					String fname1_hash = FileRenameHash(fname1, type);
					long fname1_size = copyFile(thumb_dir, fname1, uploadDir+path, fname1_hash);
					
					/* mod */
					String modoid = drawIdgenService.getNextStringId();
					insertMod.put("oid", modoid);
					insertMod.put("caroid", getMap.get("caroid"));
					insertMod.put("prttypeoid", getMap.get("prttypeoid"));
					insertMod.put("dno", getMap.get("dno"));
					insertMod.put("mversion", getMap.get("mversion"));
					insertMod.put("moduletype", getMap.get("moduletype"));
					insertMod.put("modtypeoid", getMap.get("modtypeoid"));
					insertMod.put("eono", getMap.get("eono"));
					insertMod.put("dscoid", getMap.get("dscoid"));
					insertMod.put("modsizeoid", getMap.get("modsizeoid"));
					insertMod.put("devstep", getMap.get("devstep"));
					insertMod.put("staoid", "CCN00192");
					insertMod.put("humid", humid);
					insertMod.put("dnam", getMap.get("dnam"));
					drawMngDAO.insertOnlyOneMod(insertMod);
					drawrel += modoid + ";" + getMap.get("verprt") + "|"; 	// drawrel 테이블 insert 위함
					
					/* modfiles - 도면 */
	    			String fileoid = drawFileIdgenService.getNextStringId();
	    			insertModFiles.put("oid", fileoid);
	    			insertModFiles.put("modoid", modoid);
	    			insertModFiles.put("version", "0");
	    			insertModFiles.put("filename", fname1_hash);
	    			insertModFiles.put("rfilename", fname1);
	    			insertModFiles.put("filesize", fname1_size);
	    			insertModFiles.put("masterflag", "T");
	    			insertModFiles.put("humid", humid);
	    			insertModFiles.put("filepath", path);
	    			insertModFiles.put("indexno", 1);
					drawMngDAO.insertModfiles2(insertModFiles);
					
					/* .jpg, .CATDrawing, .pdf, .wrl 신규등록 (modfiles 테이블) */
					for (int a=0; a<arrType.length; a++) {
						String fname2 = front_fname + arrType[a];
						String filepath2 = thumb_dir + File.separator + fname2;
						File f2 = new File(filepath2);
						if(f2.exists()){
							Map<String,Object> tmpModfiles = new HashMap<String,Object>();
							String fileoid2 = drawFileIdgenService.getNextStringId();
							String fname2_hash = FileRenameHash(fname2, arrType[a]);
							long fname2_size = copyFile(thumb_dir, fname2, uploadDir+path, fname2_hash);
							String indexno = drawMngDAO.selectMaxIndexNo(modoid);
							
							tmpModfiles.put("oid", fileoid2);
							tmpModfiles.put("modoid", modoid);
							tmpModfiles.put("version", "0");
							tmpModfiles.put("filename", fname2_hash);
							tmpModfiles.put("rfilename", fname2);
							tmpModfiles.put("filesize", fname2_size);
							tmpModfiles.put("masterflag", "F");
							tmpModfiles.put("humid", humid);
							tmpModfiles.put("filepath", path);
							tmpModfiles.put("indexno", String.valueOf(Integer.parseInt(indexno)+1));
							drawMngDAO.insertModfiles2(tmpModfiles);
						}
					}
				}
				
				// DB insert (DRAWREL)
				String[] arrDatas2 = drawrel.split("\\|");
				for(int i=0; i<arrDatas2.length; i++){
					String[] tempData = arrDatas2[i].split(";");
					Map<String, Object> tempMap = new HashMap<String, Object>();
					String modoid = tempData[0];
					String reloid = drawMngDAO.selectMaxVerPno(tempData[1]);
					tempMap.put("modoid", modoid);
					tempMap.put("reloid", reloid);
					tempMap.put("ref", "F");
					drawMngDAO.checkoutRevisionDrawRel(tempMap);
				}
				
				msg = "Success";
			}catch(Exception e){
				e.printStackTrace();
			}
			return msg;
		}
		
		
		//CAD IG에서 이미지 정보 조회
		public List<Map<String, Object>> selectCadigDrawInfo(HashMap<String, Object> map) throws Exception{
			return drawMngDAO.selectCadigDrawInfo(map); 
		}
		
		
		// 개정 222222
		public String checkoutRevisionMod2(List<Map<String, Object>> map) throws Exception{
			// List<Map<String, Object>> resultMap = new ArrayList<Map<String, Object>>();			// 파트 oid, 파트 newoid, 최상위 flag
			String resultMsg = "Success";
			HashMap<String, Object> oldNewOid = new HashMap<String, Object>();
			HashMap<String, Object> modprtpno = new HashMap<String, Object>();
			HashMap<String,Object> pnoSet = new HashMap<String,Object>();
			
			try{
				/* modfiles 테이블 개정용 */
				Map<String, Object> modfilesInfo = (Map)map.get(0);
				String chkhumid = (String)modfilesInfo.get("chkhumid");
				String chkout_path = (String)modfilesInfo.get("chkout_path");
				String root_oid = (String)modfilesInfo.get("root_oid");
				
				/* EBOM 테이블 개정용  */
				List<Map<String,Object>> insertEbom = new ArrayList<Map<String,Object>>();
				List<Map<String, Object>> resEBOM = null;
				Map<String, Object> rootMap = new HashMap<String, Object>();
				rootMap.put("parentoid", root_oid);
				List<Map<String, Object>> getOriEbom = drawMngDAO.selectLatestEbomChild(rootMap);
				
				/* mod 테이블 개정 */
				for(int i=0; i < map.size(); i++){
					Map<String, Object> getMap = (Map)map.get(i);
					Map<String, Object> putMap = new HashMap<String, Object>();
					String newoid = drawIdgenService.getNextStringId();
					putMap.put("newoid", newoid);
					putMap.put("oid", getMap.get("oid"));
					putMap.put("dno", getMap.get("dno"));
					putMap.put("num", getMap.get("num"));
					putMap.put("step", getMap.get("step"));
					putMap.put("eono", getMap.get("eono"));
					putMap.put("loginid", getMap.get("chkhumid"));
					oldNewOid.put((String)getMap.get("oid"), newoid);
					modprtpno.put((String)getMap.get("oid"), (String)getMap.get("prtpno"));
					drawMngDAO.checkoutRevisionMod(putMap);
					// drawMngDAO.updateModStaoid(putMap);		// 기존꺼 staoid 승인상태로				
					
					/* ebom modoid의 dno를 가진 oid 전부 LASTMODOID 변경해주기  - 2020.05.19 추가 */
					drawMngDAO.updateEbomLastmodoid(putMap);
				}
				
				/* modfiles 테이블 개정 */
				String new_root_oid = (String)oldNewOid.get(root_oid);
				Set key = oldNewOid.keySet();
				for (Iterator iterator = key.iterator(); iterator.hasNext();){
					String key_oldoid = (String)iterator.next();
	                String value_newoid = (String)oldNewOid.get(key_oldoid);
	                
	                List<Map<String, Object>> getModFiles = drawMngDAO.selectgetModFiles(key_oldoid);
	                for(int k=0; k < getModFiles.size(); k++){
						Map<String,Object> fileMap = (Map)getModFiles.get(k);
						Map<String,Object> updateModFiles = new HashMap<String,Object>();
						String fileOid = drawFileIdgenService.getNextStringId();
						String modoid = key_oldoid;
						String filename = (String)fileMap.get("filename");
						String newoid = value_newoid;
						
						updateModFiles.put("fileOid", fileOid);
						updateModFiles.put("modoid", modoid);
						updateModFiles.put("filename", filename);
						updateModFiles.put("newoid", newoid);
						updateModFiles.put("chkhumid", chkhumid);
						updateModFiles.put("chkout_path", chkout_path);
						updateModFiles.put("root_oid", new_root_oid);
						drawMngDAO.checkoutRevisionModFiles(updateModFiles);
					}
				}
				
				/* EBOM 테이블 개정  */
				// 가져온 getOriEbom 최신버전으로 가공 추가 : 2020-05-21
				HashMap<String, Object> cur_last_oid = new HashMap<String, Object>();
				for (int i=0; i<getOriEbom.size(); i++) {
					Map<String, Object> getMap = (Map)getOriEbom.get(i);
					if (getMap.get("lastmodoid") != null && !getMap.get("lastmodoid").equals(getMap.get("modoid"))){
						String cur_modoid = (String)getMap.get("modoid");
						String last_modoid = (String)getMap.get("lastmodoid");
						cur_last_oid.put(cur_modoid, last_modoid);
					}
				}
				
				for (Map<String, Object> row : getOriEbom) {
					boolean chgFlag = false;
					if (row.get("parentoid") != null && !row.get("parentoid").equals("")) {
						String modoid = (String)row.get("modoid");
						String parentoid = (String)row.get("parentoid");
						if (cur_last_oid.containsKey(modoid) == true) {
							modoid = (String)cur_last_oid.get(modoid);
							chgFlag = true;
						}
						if (cur_last_oid.containsKey(parentoid) == true) {
							parentoid = (String)cur_last_oid.get(parentoid);
							chgFlag = true;
						}
						if (chgFlag) {
							row.put("modoid", modoid);
							row.put("parentoid", parentoid);
						}
					}
				}// 가공끝
				
				// 가공된 EBOM 개정
				for (int e=1; e < getOriEbom.size(); e++){
					boolean chgFlag = false;
					Map<String, Object> oriEbomMap = (Map)getOriEbom.get(e);
					String level = String.valueOf(oriEbomMap.get("level"));
					if(level != null && level.equals("1")) break;
					if (oriEbomMap.get("parentoid") != null && !oriEbomMap.get("parentoid").equals("")){
						Map<String, Object> tempMap = new HashMap<String, Object>();
						String modoid = (String)oriEbomMap.get("modoid");
						String parentoid = (String)oriEbomMap.get("parentoid");
						if (oldNewOid.containsKey(modoid) == true) {
							modoid = (String)oldNewOid.get(modoid);
							chgFlag = true;
						}
						if (oldNewOid.containsKey(parentoid) == true) {
							parentoid = (String)oldNewOid.get(parentoid);
							chgFlag = true;
						}
						if (chgFlag) {
							tempMap.put("modoid", modoid);
							tempMap.put("parentoid", parentoid);
							tempMap.put("seq", String.valueOf(oriEbomMap.get("seq")));
							tempMap.put("humid", chkhumid);
							tempMap.put("lastmodoid", modoid);
							insertEbom.add(tempMap);
						}
					}
				}
				
				// 중복 제거 -> 반제품일때 level 1로 밑에 또붙기때문
				resEBOM = new ArrayList<Map<String, Object>>(new HashSet<Map<String, Object>>(insertEbom));
				if(resEBOM.size()>0){
					drawMngDAO.checkoutRevisionEBOM(resEBOM);
				}
				
				/* verprt, drawrel 개정 2222 : mbom 개정 방식 변경으로 인해 불필요한거 제거 */
				List<String> prtoidList = new ArrayList<String>();
				HashMap<String, Object> oldNewPrtoid = new HashMap<String, Object>();
				Set key2 = oldNewOid.keySet();   
				for (Iterator iterator = key2.iterator(); iterator.hasNext();){
					String key_oldoid = (String) iterator.next();
	                String value_newoid = (String) oldNewOid.get(key_oldoid);
	                String prtpnos = (String) modprtpno.get(key_oldoid);
	                String[] arrPrtpno = prtpnos.split(",");
	                
	                for (int k=0; k<arrPrtpno.length; k++){
	                	String prtpno = arrPrtpno[k];
	                	
	                	if (pnoSet.containsKey(prtpno)){
	                		System.out.println("------ 중복임 ");
	                	}else{
	                		String newprtoid = verPartIdgenService.getNextStringId();
	                		pnoSet.put(prtpno, newprtoid);
	                		Map<String,Object> updateVerprt = new HashMap<String,Object>();
		                	// 파트번호 중 개정 max 값 찾아서 +1
			                List<Map<String, Object>> getMaxPversion = drawMngDAO.selectMaxPrtVersion2(prtpno);
			                String pver = getMaxPversion.get(0).get("pversion").toString();
			                String prtoid = getMaxPversion.get(0).get("oid").toString();
			                int nextVer = Integer.parseInt(pver) + 1;
			                			                
			                updateVerprt.put("humid", chkhumid);
			                updateVerprt.put("pver", String.valueOf(nextVer));
			                updateVerprt.put("oid", prtoid);
			                updateVerprt.put("newoid", newprtoid);
			                oldNewPrtoid.put(prtoid, newprtoid);
			                prtoidList.add(prtoid);

			                drawMngDAO.checkoutRevisionVerprt(updateVerprt);
			                drawMngDAO.updateVerprtStaoid(updateVerprt);			// 기존꺼 승인 상태로
			                
			                /*if (key_oldoid.equals(root_oid)){
			                	updateVerprt.put("masterflag", "T");
			                }else{
			                	updateVerprt.put("masterflag", "F");
			                }
			                updateVerprt.put("chkhumid", chkhumid);
			                resultMap.add(updateVerprt);		*/	                			               
	                	}
	                	
	                	Map<String,Object> insertDrawRel = new HashMap<String,Object>();
		                insertDrawRel.put("modoid", value_newoid);
		                insertDrawRel.put("reloid", (String)pnoSet.get(prtpno));
		                insertDrawRel.put("ref", "F");
		                drawMngDAO.checkoutRevisionDrawRel(insertDrawRel);
	                	
	                }
				}
				
				/* MBOM global refresh 개정 */
				/* parentoid, child&parentoid 는 insert 해주고 child만 개정되는 경우는 update 해줘야함 - 20200622 수정*/
				List<Map<String, Object>> insertMbom = new ArrayList<Map<String, Object>>();			
				List<Map<String, Object>> refreshMbom = null;
				List<Map<String, Object>> getOriMbom = drawMngDAO.selectCurMbom(prtoidList);
				
				for ( int m=0; m < getOriMbom.size(); m++ ){
					boolean chgFlag = false;
					boolean chgFlag2 = false;
					Map<String, Object> getMbom = getOriMbom.get(m);
					if (getMbom.get("parentoid") != null && !getMbom.get("parentoid").equals("")) {
						Map<String, Object> tmpMap2 = new HashMap<String, Object>();
						Map<String, Object> deleteMap = new HashMap<String, Object>();
						String verprtoid2 = (String)getMbom.get("verprtoid");
						String parentoid2 = (String)getMbom.get("parentoid");
						
						if (oldNewPrtoid.containsKey(verprtoid2) == true) {
							verprtoid2 = (String)oldNewPrtoid.get(verprtoid2);
							chgFlag2 = true;
						}
						if (oldNewPrtoid.containsKey(parentoid2) == true) {
							parentoid2 = (String)oldNewPrtoid.get(parentoid2);
							chgFlag2 = false;
							chgFlag = true;
						}
						
						if (chgFlag2) {
							tmpMap2.put("verprtoid", verprtoid2);
							tmpMap2.put("parentoid", parentoid2);
							tmpMap2.put("quantity", (String)getMbom.get("quantity"));
							tmpMap2.put("seq", String.valueOf(getMbom.get("seq")));
							tmpMap2.put("humid", chkhumid);
							insertMbom.add(tmpMap2);
							
							// 기존 mbom 삭제하기 위함 -> 변경 : 모든경우에 삭제하면 안됨.. 이건 child만 개정되었을때
							deleteMap.put("verprtoid", (String)getMbom.get("verprtoid"));
							deleteMap.put("parentoid", (String)getMbom.get("parentoid"));
							deleteMap.put("quantity", (String)getMbom.get("quantity"));
							deleteMap.put("seq", String.valueOf(getMbom.get("seq")));
							drawMngDAO.deleteMbomList(deleteMap);
							continue;
						}
						
						if (chgFlag) {
							tmpMap2.put("verprtoid", verprtoid2);
							tmpMap2.put("parentoid", parentoid2);
							tmpMap2.put("quantity", (String)getMbom.get("quantity"));
							tmpMap2.put("seq", String.valueOf(getMbom.get("seq")));
							tmpMap2.put("humid", chkhumid);
							insertMbom.add(tmpMap2);
							
							// 기존 mbom 삭제하기 위함 -> 변경 : 모든경우에 삭제하면 안됨.. 이건 child만 개정되었을때
							/*deleteMap.put("verprtoid", (String)getMbom.get("verprtoid"));
							deleteMap.put("parentoid", (String)getMbom.get("parentoid"));
							deleteMap.put("quantity", (String)getMbom.get("quantity"));
							deleteMap.put("seq", String.valueOf(getMbom.get("seq")));
							drawMngDAO.deleteMbomList(deleteMap);*/
						}
					}
				}
				/*for ( int m=0; m < getOriMbom.size(); m++ ){
					boolean chgFlag = false;
					Map<String, Object> getMbom = getOriMbom.get(m);
					if (getMbom.get("parentoid") != null && !getMbom.get("parentoid").equals("")) {
						Map<String, Object> tmpMap2 = new HashMap<String, Object>();
						Map<String, Object> deleteMap = new HashMap<String, Object>();
						String verprtoid2 = (String)getMbom.get("verprtoid");
						String parentoid2 = (String)getMbom.get("parentoid");
						
						if (oldNewPrtoid.containsKey(verprtoid2) == true) {
							verprtoid2 = (String)oldNewPrtoid.get(verprtoid2);
							chgFlag = true;
						}
						if (oldNewPrtoid.containsKey(parentoid2) == true) {
							parentoid2 = (String)oldNewPrtoid.get(parentoid2);
							chgFlag = true;
						}
						if (chgFlag) {
							tmpMap2.put("verprtoid", verprtoid2);
							tmpMap2.put("parentoid", parentoid2);
							tmpMap2.put("quantity", (String)getMbom.get("quantity"));
							tmpMap2.put("seq", String.valueOf(getMbom.get("seq")));
							tmpMap2.put("humid", chkhumid);
							insertMbom.add(tmpMap2);
							
							// 기존 mbom 삭제하기 위함
							deleteMap.put("verprtoid", (String)getMbom.get("verprtoid"));
							deleteMap.put("parentoid", (String)getMbom.get("parentoid"));
							deleteMap.put("quantity", (String)getMbom.get("quantity"));
							deleteMap.put("seq", String.valueOf(getMbom.get("seq")));
							drawMngDAO.deleteMbomList(deleteMap);
						}
					}
				}*/
				// 중복 제거
				refreshMbom = new ArrayList<Map<String, Object>>(new HashSet<Map<String, Object>>(insertMbom));
				if(refreshMbom.size()>0){
					drawMngDAO.revisionMbom(refreshMbom);		//checkoutRevisionEBOM
				}
			}catch(Exception e){
				e.printStackTrace();
	    		//resultMap = null;
				resultMsg = "error";
	    	}
			
			return resultMsg;
		}
		
		// 개정
		public List<Map<String, Object>> checkoutRevisionMod(List<Map<String, Object>> map) throws Exception{
			List<Map<String, Object>> resultMap = new ArrayList<Map<String, Object>>();			// 파트 oid, 파트 newoid, 최상위 flag
			HashMap<String, Object> oldNewOid = new HashMap<String, Object>();
			
			try{
				/* mod 테이블 개정 */
				for(int i=0; i < map.size(); i++){
					Map<String, Object> getMap = (Map)map.get(i);
					Map<String, Object> putMap = new HashMap<String, Object>();
					String newoid = drawIdgenService.getNextStringId();
					putMap.put("newoid", newoid);
					putMap.put("oid", getMap.get("oid"));
					putMap.put("dno", getMap.get("dno"));
					putMap.put("num", getMap.get("num"));
					putMap.put("step", getMap.get("step"));
					putMap.put("loginid", getMap.get("chkhumid"));
					oldNewOid.put((String)getMap.get("oid"), newoid);
					drawMngDAO.checkoutRevisionMod(putMap);
					// drawMngDAO.updateModStaoid(putMap);		// 기존꺼 staoid 승인상태로
				}
				
				/* modfiles 테이블 개정 */
				Map<String, Object> modfilesInfo = (Map)map.get(0);
				String chkhumid = (String)modfilesInfo.get("chkhumid");
				String chkout_path = (String)modfilesInfo.get("chkout_path");
				String root_oid = (String)modfilesInfo.get("root_oid");
				String new_root_oid = (String)oldNewOid.get(root_oid);
				
				Set key = oldNewOid.keySet();
				for (Iterator iterator = key.iterator(); iterator.hasNext();){
					String key_oldoid = (String) iterator.next();
	                String value_newoid = (String) oldNewOid.get(key_oldoid);
	                
	                List<Map<String, Object>> getModFiles = drawMngDAO.selectgetModFiles(key_oldoid);
	                for(int k=0; k < getModFiles.size(); k++){
						Map<String,Object> fileMap = (Map)getModFiles.get(k);
						Map<String,Object> updateModFiles = new HashMap<String,Object>();
						String fileOid = drawFileIdgenService.getNextStringId();
						String modoid = key_oldoid;
						String filename = (String)fileMap.get("filename");
						String newoid = value_newoid;
						
						updateModFiles.put("fileOid", fileOid);
						updateModFiles.put("modoid", modoid);
						updateModFiles.put("filename", filename);
						updateModFiles.put("newoid", newoid);
						updateModFiles.put("chkhumid", chkhumid);
						updateModFiles.put("chkout_path", chkout_path);
						updateModFiles.put("root_oid", new_root_oid);
						drawMngDAO.checkoutRevisionModFiles(updateModFiles);
					}
				}
				
				/* EBOM 테이블 개정  - 2020-01-28
				List<Map<String, Object>> getOriEbom = drawMngDAO.selectgetOriEbom(root_oid);
				List<String> oidList = new ArrayList<String>();
				for (int p=0; p < getOriEbom.size(); p++){
					String oriEbomModoid = (String)getOriEbom.get(p).get("modoid");
					oidList.add(oriEbomModoid);
				}
				List<Map<String, Object>> getOriEbom2 = drawMngDAO.selectEbomModOid(oidList);
				List<Map<String,Object>> insertEbom = new ArrayList<Map<String,Object>>();
				List<Map<String, Object>> resEBOM = null;
				
				for (int e=0; e < getOriEbom2.size(); e++){
					Map<String, Object> oriEbomMap = (Map)getOriEbom2.get(e);
					if (oriEbomMap.get("parentoid") != null && !oriEbomMap.get("parentoid").equals("")){
						Map<String, Object> tempMap = new HashMap<String, Object>();
						String modoid = (String)oriEbomMap.get("modoid");
						String parentoid = (String)oriEbomMap.get("parentoid");
						
						if (oldNewOid.containsKey(modoid) == true) {
							modoid = (String)oldNewOid.get(modoid);
						}
						if (oldNewOid.containsKey(parentoid) == true) {
							parentoid = (String)oldNewOid.get(parentoid);
						}
						
						tempMap.put("modoid", modoid);
						tempMap.put("parentoid", parentoid);
						tempMap.put("seq", String.valueOf(oriEbomMap.get("seq")));
						tempMap.put("humid", chkhumid);
						insertEbom.add(tempMap);
					}
				}
				// 중복 제거
				resEBOM = new ArrayList<Map<String, Object>>(new HashSet<Map<String, Object>>(insertEbom));
				if(resEBOM.size()>0){
					drawMngDAO.deleteEbomList(oidList);
					drawMngDAO.checkoutRevisionEBOM(resEBOM);
				}*/
				
				/* EBOM 테이블 개정  */
				List<Map<String, Object>> getOriEbom = drawMngDAO.selectgetOriEbom(root_oid);
				List<String> oidList = new ArrayList<String>();
				for (int p=0; p < getOriEbom.size(); p++){
					String oriEbomModoid = (String)getOriEbom.get(p).get("modoid");
					oidList.add(oriEbomModoid);
				}
				List<Map<String, Object>> getOriEbom2 = drawMngDAO.selectEbomModOid(oidList);
				List<Map<String,Object>> insertEbom = new ArrayList<Map<String,Object>>();
				List<Map<String, Object>> resEBOM = null;
				
				for (int e=0; e < getOriEbom2.size(); e++){
					boolean chgFlag = false;
					Map<String, Object> oriEbomMap = (Map)getOriEbom2.get(e);
					if (oriEbomMap.get("parentoid") != null && !oriEbomMap.get("parentoid").equals("")){
						Map<String, Object> tempMap = new HashMap<String, Object>();
						String modoid = (String)oriEbomMap.get("modoid");
						String parentoid = (String)oriEbomMap.get("parentoid");
						
						if (oldNewOid.containsKey(modoid) == true) {
							modoid = (String)oldNewOid.get(modoid);
							chgFlag = true;
						}
						if (oldNewOid.containsKey(parentoid) == true) {
							parentoid = (String)oldNewOid.get(parentoid);
							chgFlag = true;
						}
						
						if (chgFlag) {
							tempMap.put("modoid", modoid);
							tempMap.put("parentoid", parentoid);
							tempMap.put("seq", String.valueOf(oriEbomMap.get("seq")));
							tempMap.put("humid", chkhumid);
							tempMap.put("lastmodoid", modoid);
							insertEbom.add(tempMap);
						}
					}
				}
				// 중복 제거
				resEBOM = new ArrayList<Map<String, Object>>(new HashSet<Map<String, Object>>(insertEbom));
				if(resEBOM.size()>0){
					// drawMngDAO.deleteEbomList(oidList);
					drawMngDAO.checkoutRevisionEBOM(resEBOM);
				}
				
				/* verprt, drawrel 개정 */			
				Set key2 = oldNewOid.keySet();
				for (Iterator iterator = key2.iterator(); iterator.hasNext();){
					String key_oldoid = (String) iterator.next();
	                String value_newoid = (String) oldNewOid.get(key_oldoid);
	                
	                List<Map<String, Object>> drawrelMap = drawMngDAO.selectdrawrel(key_oldoid);	                
	                Map<String, Object> drawRel = (Map)drawrelMap.get(0);
	                String prtoid = (String)drawRel.get("reloid");
	                String ref = (String)drawRel.get("ref");
	                String newprtoid = verPartIdgenService.getNextStringId();
	                
	                Map<String,Object> updateVerprt = new HashMap<String,Object>();
	                updateVerprt.put("oid", prtoid);
	                updateVerprt.put("newoid", newprtoid);
	                
	                // 파트번호 중 개정 max 값 찾아서 +1
	                List<Map<String, Object>> getMaxPversion = drawMngDAO.selectMaxPrtVersion(prtoid);
	                // String reghumid = getMaxPversion.get(0).get("humid").toString();
	                String pver = getMaxPversion.get(0).get("pversion").toString();
	                int nextVer = Integer.parseInt(pver) + 1;
	                //updateVerprt.put("humid", reghumid);
	                updateVerprt.put("humid", chkhumid);
	                updateVerprt.put("pver", String.valueOf(nextVer));
	                
	                drawMngDAO.checkoutRevisionVerprt(updateVerprt);
	                drawMngDAO.updateVerprtStaoid(updateVerprt);		// 기존꺼 승인 상태로
	                
	                if (key_oldoid.equals(root_oid)){
	                	updateVerprt.put("masterflag", "T");
	                }else{
	                	updateVerprt.put("masterflag", "F");
	                }
	                updateVerprt.put("chkhumid", chkhumid);
	                resultMap.add(updateVerprt);
	                
	                Map<String,Object> insertDrawRel = new HashMap<String,Object>();
	                insertDrawRel.put("modoid", value_newoid);
	                insertDrawRel.put("reloid", newprtoid);
	                insertDrawRel.put("ref", ref);
	                drawMngDAO.checkoutRevisionDrawRel(insertDrawRel);
				}
			}catch(Exception e){
				e.printStackTrace();
	    		resultMap = null;
	    	}
			
			return resultMap;
		}
		
		public void updateCallBomMain(Map<String, Object> map) throws Exception {
			drawMngDAO.updateCallBomMain(map);
		}

		public String procPdmMig(Map<String, Object> map) throws Exception{
			String result = "success";
			try{
				/* 파트 마이그레이션
				 * 
				 */
				/*
				System.out.println("====================파트 마이그레이션 시작 ========================");
				List<Map<String, Object>> tempVerPrtList = (List<Map<String, Object>>)drawMngDAO.selectTempVerPrtList(map);
				if(tempVerPrtList != null){
					for(int i = 0; i < tempVerPrtList.size(); i++){
						Map tempVerPrtMap = (Map)tempVerPrtList.get(i);
						String oid = (String)tempVerPrtMap.get("oid");
						//MBOM 최신버전만 달리도록 체크 수정
						List<Map<String, Object>> bomChkInfoList = (List<Map<String, Object>>)drawMngDAO.selectMBomCheckList(tempVerPrtMap);
						if(bomChkInfoList != null && bomChkInfoList.size() > 0){
							for(int j = 0; j < bomChkInfoList.size(); j++){
								Map bomChkInfoMap = (Map)bomChkInfoList.get(j);
								drawMngDAO.deleteMBomInfo(bomChkInfoMap);
							}
						}

						String factorys = "";
						String factory = (String)tempVerPrtMap.get("factory");
						String[] organization_codes = null;
						if(factory != null && !factory.equals("")){
							if(factory.indexOf(",") != -1){
								organization_codes = factory.split("\\,");
							}else{
								organization_codes = new String[1];
								organization_codes[0] = factory.trim();
							}
						}
						if(organization_codes != null && organization_codes.length > 0){
							for(int j = 0; j < organization_codes.length; j++){
								if(j == 0){
									if(organization_codes[j] != null && organization_codes[j].equals("JN1")){
										factorys="CCN11685";
									}else if(organization_codes[j] != null && organization_codes[j].equals("NY1")){
										factorys="CCN11686";
									}else if(organization_codes[j] != null && organization_codes[j].equals("NY2")){
										factorys="CCN11700";
									}else if(organization_codes[j] != null && organization_codes[j].equals("NY3")){
										factorys="CCN11701";
									}else if(organization_codes[j] != null && organization_codes[j].equals("NY4")){
										factorys="CCN11702";
									}else if(organization_codes[j] != null && organization_codes[j].equals("NYP")){
										factorys="CCN11703";
									}
								}else{
									if(organization_codes[j] != null && organization_codes[j].equals("JN1")){
										factorys=factorys+",CCN11685";
									}else if(organization_codes[j] != null && organization_codes[j].equals("NY1")){
										factorys=factorys+",CCN11686";
									}else if(organization_codes[j] != null && organization_codes[j].equals("NY2")){
										factorys=factorys+",CCN11700";
									}else if(organization_codes[j] != null && organization_codes[j].equals("NY3")){
										factorys=factorys+",CCN11701";
									}else if(organization_codes[j] != null && organization_codes[j].equals("NY4")){
										factorys=factorys+",CCN11702";
									}else if(organization_codes[j] != null && organization_codes[j].equals("NYP")){
										factorys=factorys+",CCN11703";
									}
								}
							}
						}
						tempVerPrtMap.put("factory", factorys);
						drawMngDAO.insertVerPrtMigInfo(tempVerPrtMap);
						
						
						
					}
				}
				System.out.println("====================파트 마이그레이션 끝 =========================");
				*/
				
				/* 도면 마이그레이션
				 * 
				 */
				/*
				System.out.println("====================도면 마이그레이션 시작 ========================");
				List<Map<String, Object>> tempModList = (List<Map<String, Object>>)drawMngDAO.selectTempModList(map);
				if(tempModList != null){
					for(int i = 0; i < tempModList.size(); i++){
						System.out.println(i);
						Map tempModMap = (Map)tempModList.get(i);
				*/		
						//EBOM SEQUENCE 넣기
						/*
						List<Map<String, Object>> ebomInfoList = (List<Map<String, Object>>)drawMngDAO.selectEBomCheckList(tempModMap);
						if(ebomInfoList != null && ebomInfoList.size() > 0){
							for(int j = 0; j < ebomInfoList.size(); j++){
								Map ebomInfoMap = (Map)ebomInfoList.get(j);
								ebomInfoMap.put("seq", j+1);
								drawMngDAO.updateEbomChgSeq(ebomInfoMap);
							}
						}
						*/
						/*
						//EBOM 최신버전만 달리도록 체크 수정
						List<Map<String, Object>> bomChkInfoList = (List<Map<String, Object>>)drawMngDAO.selectEBomCheckList(tempModMap);
						if(bomChkInfoList != null && bomChkInfoList.size() > 0){
							for(int j = 0; j < bomChkInfoList.size(); j++){
								Map bomChkInfoMap = (Map)bomChkInfoList.get(j);
								drawMngDAO.deleteEBomInfo(bomChkInfoMap);
							}
						}
						*/
						/*
						drawMngDAO.insertModMigInfo(tempModMap);
						List<Map<String, Object>> tempModfileList = (List<Map<String, Object>>)drawMngDAO.selectTempModfileList(tempModMap);
						if(tempModfileList != null){
							for(int f = 0; f < tempModfileList.size(); f++){
								Map tempModfileMap = (Map)tempModfileList.get(f);
								String fileOid = drawFileIdgenService.getNextStringId();
								tempModfileMap.put("oid", fileOid);
								drawMngDAO.insertModfileMigInfo(tempModfileMap);
							}
						}*/
				/*
					}
				} 
				System.out.println("====================도면 마이그레이션 끝 =========================");
				*/
				
				/* 설변 마이그레이션
				 * 
				 */
				/*
				System.out.println("====================설변 마이그레이션 시작 ========================");
				List<Map<String, Object>> tempEcList = (List<Map<String, Object>>)drawMngDAO.selectTempEcList(map);
				if(tempEcList != null){
					for(int i = 0; i < tempEcList.size(); i++){
						Map tempEcMap = (Map)tempEcList.get(i);
						drawMngDAO.insertEcMigInfo(tempEcMap);
						drawMngDAO.insertEcContMigInfo(tempEcMap);
						List<Map<String, Object>> tempEcrelInfoList = (List<Map<String, Object>>)drawMngDAO.selectTempEcrelInfoList(tempEcMap);
						if(tempEcrelInfoList != null){
							for(int e = 0; e < tempEcrelInfoList.size(); e++){
								Map tempEcrelInfoMap = (Map) tempEcrelInfoList.get(e);
								drawMngDAO.insertEcRelInfoMigInfo(tempEcrelInfoMap);
							}
						}
					}
				}
				System.out.println("====================설변 마이그레이션 끝 =========================");
				*/
			}catch(Exception e){
				e.printStackTrace();
			}
			
			return result;
		}
		
		public String updateDrawRelPartRevProcess2(List<Map<String,Object>> paramList) throws Exception{
			/*
			* oldoid: VPR98730006, newoid: VPR00001857, flag: F, ID: admin
			* oldoid: VPR98730009, newoid: VPR00001858, flag: F, ID: admin
			* oldoid: VPR98730001, newoid: VPR00001859, flag: T, ID: admin
			* oldoid: VPR98730002, newoid: VPR00001860, flag: T, ID: admin
			* oldoid: VPR98730005, newoid: VPR00001861, flag: T, ID: admin
			* oldoid: VPR98730007, newoid: VPR00001862, flag: T, ID: admin
			*/
			
			
			String result = "Success";
			try{
				/* mbom 구조 가져오기 */
				
			    if(paramList != null){
			    	for(int k = 0; k < paramList.size(); k++){
			    		List<Map<String, Object>> bomInfoList = null;
			    		String oldoid_mst = "";
						String newoid_mst = "";
						String chkhumid = "";
			    		
			    		Map paramMap1 = (Map)paramList.get(k);
						String masterflag1 = (String)paramMap1.get("masterflag");
						if(masterflag1 != null && masterflag1.equals("T")){
							oldoid_mst = (String)paramMap1.get("oid");
							newoid_mst = (String)paramMap1.get("newoid");
							chkhumid = (String)paramMap1.get("chkhumid");
							HashMap<String, String> bomItemParamMap1 = new HashMap<String, String>();
							bomItemParamMap1.put("verprtoid",(String)paramMap1.get("oid"));
							bomInfoList = (List<Map<String, Object>>)partMngDAO.selectMbomTreeList(bomItemParamMap1);
							paramList.remove(k);
							//break;
							
							TreeSet<String> rowOldTreeSet = new TreeSet<String>();
							if(bomInfoList != null){
								for (int e=1; e < bomInfoList.size(); e++){
									Map bomInfoMap = (Map)bomInfoList.get(e);
									Map<String,Object> newMbomMap = new HashMap<String,Object>();
									String verprtoid = (String)bomInfoMap.get("verprtoid");
									String parentoid = (String)bomInfoMap.get("parentoid");
									String level = String.valueOf(bomInfoMap.get("level"));
									if(level != null && level.equals("1")) break;
									if(oldoid_mst != null && oldoid_mst.equals(parentoid)){
										newMbomMap.put("verprtoid", verprtoid);
										newMbomMap.put("parentoid", newoid_mst);
										newMbomMap.put("seq", String.valueOf(bomInfoMap.get("seq")));
										newMbomMap.put("quantity", String.valueOf(bomInfoMap.get("quantity")));
										newMbomMap.put("humid", chkhumid);  
									}
									if(paramList != null){
										for(int i = 0; i < paramList.size(); i++){
											Map paramMap = (Map)paramList.get(i);
											String masterflag = (String)paramMap.get("masterflag");
											String oldoid = (String)paramMap.get("oid");
											String newoid = (String)paramMap.get("newoid");
											if(masterflag != null && masterflag.equals("F")){
												if(oldoid_mst != null && oldoid_mst.equals(parentoid) && oldoid != null && oldoid.equals(verprtoid)){
													newMbomMap.put("verprtoid", newoid);
													rowOldTreeSet.add(oldoid+"|"+newoid);
												}
											}
										}
									}
									if (newMbomMap.get("verprtoid") != null && !newMbomMap.get("verprtoid").equals(""))
										drawMngDAO.checkoutRevisionMBOM(newMbomMap);
								}
							}
				      
							if(rowOldTreeSet != null && rowOldTreeSet.size() > 0){
								 int r=0;
								 for(String rowOid : rowOldTreeSet){
									 String[] rowOids = rowOid.split("\\|");
									 String oldoid = (String)rowOids[0];
					  				 String newoid = (String)rowOids[1];
									 HashMap<String, String> bomItemParamMap = new HashMap<String, String>();
					  				 bomItemParamMap.put("verprtoid",oldoid);
									 List<Map<String, Object>> subBomInfoList = (List<Map<String, Object>>)partMngDAO.selectMbomTreeList(bomItemParamMap);
									 if(subBomInfoList != null){
									        for (int e=1; e < subBomInfoList.size(); e++){
									        	Map subBomInfoMap = (Map)subBomInfoList.get(e);
									        	Map<String,Object> newMbomMap = new HashMap<String,Object>();
									            String verprtoid = (String)subBomInfoMap.get("verprtoid");
											    String parentoid = (String)subBomInfoMap.get("parentoid");
											    String level = String.valueOf(subBomInfoMap.get("level"));
											    if(level != null && level.equals("1")) break;
											    if(parentoid != null && parentoid.equals(oldoid)){
											    	newMbomMap.put("verprtoid", verprtoid);
						  						    newMbomMap.put("parentoid", newoid);
						  						    newMbomMap.put("seq", String.valueOf(subBomInfoMap.get("seq")));
						  						    newMbomMap.put("quantity", String.valueOf(subBomInfoMap.get("quantity")));
						  						    newMbomMap.put("humid", chkhumid);
					  						    	drawMngDAO.checkoutRevisionMBOM(newMbomMap);
											     }
									          } 
									 	}
								 	}
								}
							/*
							if(paramList != null){
								for(int i = 0; i < paramList.size(); i++){
									Map paramMap = (Map)paramList.get(i);
				  					String masterflag = (String)paramMap.get("masterflag");
				  					String oldoid = (String)paramMap.get("oid");
				  					String newoid = (String)paramMap.get("newoid");
				  					HashMap<String, String> bomItemParamMap = new HashMap<String, String>();
				  					bomItemParamMap.put("verprtoid",oldoid);
				  					List<Map<String, Object>> subBomInfoList = (List<Map<String, Object>>)partMngDAO.selectMbomTreeList(bomItemParamMap);
				  					if(bomInfoList != null){
							        for (int e=0; e < subBomInfoList.size(); e++){
							        	Map subBomInfoMap = (Map)subBomInfoList.get(e);
							        	Map<String,Object> newMbomMap = new HashMap<String,Object>();
							            String verprtoid = (String)subBomInfoMap.get("verprtoid");
									    String parentoid = (String)subBomInfoMap.get("parentoid");
									    if(parentoid != null && parentoid.equals(oldoid)){
									    	newMbomMap.put("verprtoid", verprtoid);
				  						    newMbomMap.put("parentoid", newoid);
				  						    newMbomMap.put("seq", String.valueOf(subBomInfoMap.get("seq")));
				  						    newMbomMap.put("quantity", String.valueOf(subBomInfoMap.get("quantity")));
				  						    newMbomMap.put("humid", chkhumid);  
				  						    drawMngDAO.checkoutRevisionMBOM(newMbomMap);
									     }
							          }
							       }
								}
							}
							*/							
						}
					}
				}
				
				
			}catch(Exception e){
				result="Fail";
				e.printStackTrace();
			}
			return result;
		}
		
		public String updateDrawRelPartRevProcess(List<Map<String,Object>> paramList) throws Exception{
			/*
			String result = "Success";
			List<Map<String, Object>> bomInfoList = null;
			String newoid_mst = "";
			String oldoid_mst = "";
			HashMap<String, Object> oldNewOid = new HashMap<String, Object>();
			try{
				for(int k=0; k < paramList.size(); k++){
					Map<String, Object> getMap = (Map)paramList.get(k);
					oldNewOid.put((String)getMap.get("oid"), (String)getMap.get("newoid"));
				}
				if(paramList != null){
					for(int i = 0; i < paramList.size(); i++){
						Map paramMap = (Map)paramList.get(i);
						String masterflag = (String)paramMap.get("masterflag");
						if(masterflag != null && masterflag.equals("T")){
							oldoid_mst = (String)paramMap.get("oid");
							HashMap<String, String> bomItemParamMap = new HashMap<String, String>();
							bomItemParamMap.put("verprtoid",oldoid_mst);
							bomInfoList = (List<Map<String, Object>>)partMngDAO.selectMbomTreeList(bomItemParamMap);
							break;
						}
					}
				}
				for (int e=0; e < bomInfoList.size(); e++){
					boolean chgFlag = false;
					Map<String, Object> bomInfoMap = (Map)bomInfoList.get(e);
					if (bomInfoMap.get("parentoid") != null && !bomInfoMap.get("parentoid").equals("")){
						Map<String,Object> newMbomMap = new HashMap<String,Object>();
						String verprtoid = (String)bomInfoMap.get("verprtoid");
						String parentoid = (String)bomInfoMap.get("parentoid");
						String chkhumid = (String)bomInfoMap.get("chkhumid");
						if (oldNewOid.containsKey(verprtoid) == true) {
							verprtoid = (String)oldNewOid.get(verprtoid);
							chgFlag = true;
						}
						if (oldNewOid.containsKey(parentoid) == true) {
							parentoid = (String)oldNewOid.get(parentoid);
							chgFlag = true;
						}
						if (chgFlag) {
							newMbomMap.put("verprtoid", verprtoid);
							newMbomMap.put("parentoid", parentoid);
							newMbomMap.put("seq", String.valueOf(bomInfoMap.get("seq")));
							newMbomMap.put("quantity", String.valueOf(bomInfoMap.get("quantity")));
							newMbomMap.put("humid", chkhumid);
							drawMngDAO.checkoutRevisionMBOM(newMbomMap);
						}
					}
				}
			}catch(Exception e){
				result="Fail";
				e.printStackTrace();
			}
			return result;
			*/
			
			
			/*
			* oldoid: VPR00000602, newoid: VPR00001154, flag: F, ID: admin, qty: 3
			* oldoid: VPR00000602, newoid: VPR00001155, flag: F, ID: admin, qty: 1
			* oldoid: VPR00000602, newoid: VPR00001156, flag: T, ID: admin, qty: 1 
			*/
			String result = "Success";
			String oldoid_mst = "";
			String newoid_mst = "";
			String chkhumid = "";
			try{
				/* mbom 구조 가져오기 */
				List<Map<String, Object>> bomInfoList = null;
			    if(paramList != null){
			    	for(int i = 0; i < paramList.size(); i++){
			    		Map paramMap = (Map)paramList.get(i);
						String masterflag = (String)paramMap.get("masterflag");
						if(masterflag != null && masterflag.equals("T")){
							oldoid_mst = (String)paramMap.get("oid");
							newoid_mst = (String)paramMap.get("newoid");
							chkhumid = (String)paramMap.get("chkhumid");
							HashMap<String, String> bomItemParamMap = new HashMap<String, String>();
							bomItemParamMap.put("verprtoid",(String)paramMap.get("oid"));
							bomInfoList = (List<Map<String, Object>>)partMngDAO.selectMbomTreeList(bomItemParamMap);
							paramList.remove(i);
							break;
						}
					}
				}
				
				if(bomInfoList != null){
					for (int e=0; e < bomInfoList.size(); e++){
						Map bomInfoMap = (Map)bomInfoList.get(e);
						Map<String,Object> newMbomMap = new HashMap<String,Object>();
						String verprtoid = (String)bomInfoMap.get("verprtoid");
						String parentoid = (String)bomInfoMap.get("parentoid");
						if(paramList != null){
							for(int i = 0; i < paramList.size(); i++){
								Map paramMap = (Map)paramList.get(i);
								String masterflag = (String)paramMap.get("masterflag");
								String oldoid = (String)paramMap.get("oid");
								String newoid = (String)paramMap.get("newoid");
								if(masterflag != null && masterflag.equals("F")){
									if(oldoid_mst != null && oldoid_mst.equals(parentoid) && oldoid != null && oldoid.equals(verprtoid)){
										newMbomMap.put("verprtoid", newoid);
										newMbomMap.put("parentoid", newoid_mst);
										newMbomMap.put("seq", String.valueOf(bomInfoMap.get("seq")));
										newMbomMap.put("quantity", String.valueOf(bomInfoMap.get("quantity")));
										newMbomMap.put("humid", chkhumid);
									}else if(oldoid_mst != null && oldoid_mst.equals(parentoid)){
										newMbomMap.put("verprtoid", verprtoid);
										newMbomMap.put("parentoid", newoid_mst);
										newMbomMap.put("seq", String.valueOf(bomInfoMap.get("seq")));
										newMbomMap.put("quantity", String.valueOf(bomInfoMap.get("quantity")));
										newMbomMap.put("humid", chkhumid);  
									}
									drawMngDAO.checkoutRevisionMBOM(newMbomMap);
								}
							}
						}
					}
				}
	      
				if(paramList != null){
					for(int i = 0; i < paramList.size(); i++){
						Map paramMap = (Map)paramList.get(i);
	  					String masterflag = (String)paramMap.get("masterflag");
	  					String oldoid = (String)paramMap.get("oid");
	  					String newoid = (String)paramMap.get("newoid");
	  					HashMap<String, String> bomItemParamMap = new HashMap<String, String>();
	  					bomItemParamMap.put("verprtoid",oldoid);
	  					List<Map<String, Object>> subBomInfoList = (List<Map<String, Object>>)partMngDAO.selectMbomTreeList(bomItemParamMap);
	  					if(bomInfoList != null){
				        for (int e=0; e < subBomInfoList.size(); e++){
				        	Map subBomInfoMap = (Map)subBomInfoList.get(e);
				        	Map<String,Object> newMbomMap = new HashMap<String,Object>();
				            String verprtoid = (String)subBomInfoMap.get("verprtoid");
						    String parentoid = (String)subBomInfoMap.get("parentoid");
						    if(parentoid != null && parentoid.equals(oldoid)){
						    	newMbomMap.put("verprtoid", verprtoid);
	  						    newMbomMap.put("parentoid", newoid);
	  						    newMbomMap.put("seq", String.valueOf(subBomInfoMap.get("seq")));
	  						    newMbomMap.put("quantity", String.valueOf(subBomInfoMap.get("quantity")));
	  						    newMbomMap.put("humid", chkhumid);  
	  						    drawMngDAO.checkoutRevisionMBOM(newMbomMap);
						     }
				          }
				       }
					}
				}
			}catch(Exception e){
				result="Fail";
				e.printStackTrace();
			}
			return result;
		}

		public List<Map<String, Object>> selectModfilesList(List<String> arr) throws Exception {
			return drawMngDAO.selectModfilesList(arr);
		}
		
		public List<Map<String, Object>> selectModMaxVersion(List<String> arr) throws Exception {
			return drawMngDAO.selectModMaxVersion(arr);
		}
		
		public List<Map<String, Object>> selectModDrawrel(List<String> arr) throws Exception {
			return drawMngDAO.selectModDrawrel(arr);
		}
		
		public String updateCancleStaoid(Map<String, Object> map) throws Exception {
			String msg = "";
			try{
				List<Map<String,Object>> staoidList = new ArrayList<Map<String,Object>>();
				List<String> oidList = new ArrayList<String>();
				String oids = (String)map.get("oids");
				String[] array = oids.split(";");
				for (int i=0; i<array.length; i++){
					oidList.add(array[i]);
				}
				staoidList = drawMngDAO.selectCancleStaoid(oidList);
				
				for (int k=0; k<staoidList.size(); k++){
					Map<String,Object> getMap = (Map)staoidList.get(k);
					String staoid = (String)getMap.get("staoid");
					if (staoid != null && staoid.equals("CCN00193")){
						msg = "Success";
					}else{
						msg = "other::" + (String)getMap.get("dno");
						break;
					}
				}
				
				if (msg != null && msg.equals("Success")){
					drawMngDAO.updateCancleStaoid(oidList);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			return msg;
		}
		
		public String checkMaxVersion(Map<String, Object> map) throws Exception {
			return drawMngDAO.checkMaxVersion(map);
		}
		
		public void updateModCancleCheckout(List<String> arr) throws Exception {
			drawMngDAO.updateModCancleCheckout(arr);
		}
		public void updateModFilesCancleCheckout(List<String> arr) throws Exception {
			drawMngDAO.updateModFilesCancleCheckout(arr);
		}
		
		public List<Map<String, Object>> selectMainEbomTree(Map<String, Object> map) throws Exception {
			List<Map<String, Object>> parentList = (List<Map<String, Object>>)drawMngDAO.selectMainSearchParent(map);
			String oid = (String)parentList.get(0).get("oid");
			HashMap<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("modoid",oid);
			return drawMngDAO.selectEbomTreeList(paramMap);
		}
		
		public List<Map<String, Object>> selectPartInfo(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectPartInfo(map);
		}
		
		/* 추가 등록 없이 체크인 
		 * 
	     * dynaPLM 에서 쓰던 도면들이
		 * aa.CATPart, aa.CATPart.jpg, aa.CATPart.wrl, aa.CATDrawing, aa.CATPart.pdf 
		 * 이런 형태여서 아래 코드 만듬 
		 * */
		public String updateCheckInInfo(Map<String, Object> map) throws Exception{
			String msg = "fail";
			List<Map<String, Object>> CheckInFiles = null;
			try{
				String chkout_path = (String)map.get("chkout_path");
				String rootfile = (String)map.get("rootfile");
				String thumb_dir = (String)map.get("thumb_dir");
				String staoid = (String)map.get("put_staoid");
				String humid = (String)map.get("id");
				String[] arrType = {".jpg", ".CATDrawing", ".pdf", ".wrl"};
				
				// Modfileshistory 입력
				drawMngDAO.insertModfilesHistory(chkout_path);
				
				// Modfiles & Mod update or insert
				SimpleDateFormat format = new SimpleDateFormat ("yyyyMMddHHmmss");
				String format_time = format.format (System.currentTimeMillis());
				String path = File.separator + format_time.substring(0, 4);
				Map<String, Object> modCheckIn = new HashMap<String, Object>();
				
				CheckInFiles = drawMngDAO.selectCheckInModfiles(chkout_path);
				for (int k=0; k<CheckInFiles.size(); k++){
					Map<String, Object> updateModfiles = new HashMap<String, Object>();
					Map<String, Object> getMap = (Map)CheckInFiles.get(k);
					String rfilename = (String)getMap.get("rfilename");
					String fname = rfilename.substring(0, rfilename.lastIndexOf(".")); 
					String type = rfilename.substring(rfilename.lastIndexOf("."));
					
					if (type.equals(".CATProduct") || type.equals(".CATPart")){
						/* 도면 업데이트 */
						String modoid = (String)getMap.get("modoid");
						String oid = (String)getMap.get("oid");
						String version = (String)getMap.get("version");
						String fname_hash = FileRenameHash(rfilename, type);
						long fname_size = copyFile(thumb_dir, rfilename, uploadDir+path, fname_hash);
						
						updateModfiles.put("filepath", path);
						updateModfiles.put("version", String.valueOf(Integer.parseInt(version)+1));
						updateModfiles.put("rfilename", rfilename);
						updateModfiles.put("filename", fname_hash);
						updateModfiles.put("filesize", fname_size);
						updateModfiles.put("chkoutpath", chkout_path);
						updateModfiles.put("oid", oid);
						drawMngDAO.updateModfilesCheckIn(updateModfiles);
						
						modCheckIn.put("modoid", modoid);
						modCheckIn.put("staoid", staoid);
						drawMngDAO.updateModCheckIn(modCheckIn);
						
						/* .jpg, .CATDrawing, .pdf, .wrl 업데이트 or 신규등록 */
						for (int a=0; a<arrType.length; a++) {
							Map<String, Object> getMap2 = new HashMap<String,Object>();
							String rfilename2 = "";
							boolean flag = false;
							for (int i=0; i<CheckInFiles.size(); i++){
								getMap2 = (Map)CheckInFiles.get(i);
								rfilename2 = (String)getMap2.get("rfilename");							
								if (rfilename2.contains(fname) && rfilename2.contains(arrType[a])){
									flag = true;
									break;
								}
							}
							
							String fname2 = fname + arrType[a];
							String filepath2 = thumb_dir + File.separator + fname2;
							File f2 = new File(filepath2);
							if(flag){	// 체크아웃 되있으므로 update
								Map<String,Object> tmpModfiles = new HashMap<String,Object>();
								String modoid2 = (String)getMap2.get("modoid");
								String oid2 = (String)getMap2.get("oid");
								String version2 = (String)getMap2.get("version");
								String fname_hash2 = "";
								long fname_size2 = 0;
								if(f2.exists()){			//  aa.CATPart.jpg 형태 때문에 만들어줌
									fname_hash2 = FileRenameHash(fname2, arrType[a]);
									fname_size2 = copyFile(thumb_dir, fname2, uploadDir+path, fname_hash2);
								}else{
									fname_hash2 = FileRenameHash(rfilename2, arrType[a]);
									fname_size2 = copyFile(thumb_dir, rfilename2, uploadDir+path, fname_hash2);
								}
								tmpModfiles.put("filepath", path);
								tmpModfiles.put("version", String.valueOf(Integer.parseInt(version2)+1));
								tmpModfiles.put("rfilename", fname2);				// 여기서 aa.jpg 형태로 바꿔줌 (만약 aa.CATPart.jpg 였다면)
								tmpModfiles.put("filename", fname_hash2);
								tmpModfiles.put("filesize", fname_size2);
								tmpModfiles.put("chkoutpath", chkout_path);
								tmpModfiles.put("oid", oid2);
								drawMngDAO.updateModfilesCheckIn(tmpModfiles);
							}else{		// 등록 안되어있으므로 신규등록 insert
								if(f2.exists()){
									Map<String,Object> tmpModfiles = new HashMap<String,Object>();
									String fileoid2 = drawFileIdgenService.getNextStringId();
									String fname_hash2 = FileRenameHash(fname2, arrType[a]);
									long fname_size2 = copyFile(thumb_dir, fname2, uploadDir+path, fname_hash2);
									String indexno = drawMngDAO.selectMaxIndexNo(modoid);
									
									tmpModfiles.put("oid", fileoid2);
									tmpModfiles.put("modoid", modoid);
									tmpModfiles.put("version", "0");
									tmpModfiles.put("filename", fname_hash2);
									tmpModfiles.put("rfilename", fname2);
									tmpModfiles.put("filesize", fname_size2);
									tmpModfiles.put("masterflag", "F");
									tmpModfiles.put("humid", humid);
									tmpModfiles.put("filepath", path);
									tmpModfiles.put("indexno", String.valueOf(Integer.parseInt(indexno)+1));
									drawMngDAO.insertModfiles2(tmpModfiles);
								}
							}
						}
					}
					
					// 기존 코드
//							String modoid = (String)getMap.get("modoid");					
//							String rfilename = (String)getMap.get("rfilename");
//							String oid = (String)getMap.get("oid");
//							String fname = rfilename.substring(0, rfilename.lastIndexOf(".")); 
//							String type = rfilename.substring(rfilename.lastIndexOf("."));
//							String version = (String)getMap.get("version");
//							
//							String fname_hash = FileRenameHash(rfilename, type);
//							long fname_size = copyFile(thumb_dir, rfilename, uploadDir+path, fname_hash);
//							
//							updateModfiles.put("filepath", path);
//							updateModfiles.put("version", String.valueOf(Integer.parseInt(version)+1));
//							updateModfiles.put("rfilename", rfilename);
//							updateModfiles.put("filename", fname_hash);
//							updateModfiles.put("filesize", fname_size);
//							updateModfiles.put("chkoutpath", chkout_path);
//							updateModfiles.put("oid", oid);
//							drawMngDAO.updateModfilesCheckIn(updateModfiles);
//							
//							modCheckIn.put("modoid", modoid);
//							modCheckIn.put("staoid", staoid);
//							drawMngDAO.updateModCheckIn(modCheckIn);
				}
				msg = "Success";
			}catch(Exception e){
				e.printStackTrace();
			}
			return msg;
		}
		
		public List<Map<String, Object>> autoFillModInfo(Map<String, Object> map) throws Exception {
			return drawMngDAO.autoFillModInfo(map);
		}
		
		public List<Map<String, Object>> selectComtecopseq2(Map<String, Object> map) throws Exception {
			List<Map<String, Object>> comtecopseq = null;
			try{
				String seq = (String)map.get("seq");
				BigDecimal a = new BigDecimal(seq);
				comtecopseq = drawMngDAO.selectComtecopseq2(map);
				Map<String, Object> getMap = (Map)comtecopseq.get(0);
				BigDecimal next_id = (BigDecimal)getMap.get("nextId");
				map.put("next_id", next_id.add(a));
				drawMngDAO.updateComtecopseq(map);
			}catch(Exception e){
				e.printStackTrace();
			}
			
			return comtecopseq;
		}
		
		public List<Map<String, Object>> selectRegCatchError3(Map<String, Object> map) throws Exception {
			List<String> pnoList = new ArrayList<String>();
			String pno = (String)map.get("put_pno");
			String[] array = pno.split(";");
			for (int i=0; i<array.length; i++){
				pnoList.add(array[i]);
			}
			return drawMngDAO.selectRegCatchError3(pnoList);
		}
		
		/* 체크인 - 추가파일 있을때
		 * 
	     * dynaPLM 에서 쓰던 도면들이
		 * aa.CATPart, aa.CATPart.jpg, aa.CATPart.wrl, aa.CATDrawing, aa.CATPart.pdf 
		 * 이런 형태여서 아래 코드 만듬 
		 * */
		public String insertAddEBOM(Map<String, Object> map) throws Exception{
			String msg = "fail";
			List<Map<String, Object>> CheckInFiles = null;
			HashMap<String, Object> File_Oid = new HashMap<String, Object>();
			
			try{
				String chkout_path = (String)map.get("chkout_path");
				String staoid = (String)map.get("put_staoid");
				String moddata = (String)map.get("moddata");
				String thumb_dir = (String)map.get("thumb_dir");
				String humid = (String)map.get("id");
				String drawrel = "";
				String[] arrType = {".jpg", ".CATDrawing", ".pdf", ".wrl"};
				
				List<Map<String, Object>> EBOMList = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> LinkIDList = new ArrayList<Map<String, Object>>();
				EBOMList = (List<Map<String, Object>>)map.get("EBOMList");
				LinkIDList = (List<Map<String, Object>>)map.get("LinkIDList");
				
				String[] arrDatas = moddata.split("\\|");
				List<Map<String,Object>> tmpModData = new ArrayList<Map<String,Object>>();
				for(int i=0; i<arrDatas.length; i++){
					String[] tempData = arrDatas[i].split(";");
					Map<String, Object> tempMap = new HashMap<String, Object>();
					tempMap.put("filename", tempData[0]);
					tempMap.put("caroid", tempData[1]);
					tempMap.put("prttypeoid", tempData[2]);
					tempMap.put("dno", tempData[3]);
					tempMap.put("mversion", tempData[4]);
					tempMap.put("moduletype", tempData[5]);
					tempMap.put("modtypeoid", tempData[6]);
					tempMap.put("eono", tempData[7]);
					tempMap.put("dscoid", tempData[8]);
					tempMap.put("modsizeoid", tempData[9]);
					tempMap.put("devstep", tempData[10]);
					tempMap.put("verprt", tempData[11]);
					tempMap.put("dnam", tempData[12]);
					tmpModData.add(tempMap);
				}
				
				// Modfileshistory 입력
				System.out.println("------------------------- Modfileshistory insert ----------------------------");
				drawMngDAO.insertModfilesHistory(chkout_path);
				
				/* 기존 ebom File_Oid [파일명 : modoid] - 업데이트 하기 전 정보 가져오기 */
				String chkRootOid = drawMngDAO.selectCheckInRootOid(chkout_path);
				
				// 체크아웃 해제  Modfiles & Mod update or 없던 modfiles 신규등록
				System.out.println("------------------------- (Add) Modfiles & Mod update or insert ----------------------------");
				SimpleDateFormat format = new SimpleDateFormat ("yyyyMMddHHmmss");
				String format_time = format.format (System.currentTimeMillis());
				String path = File.separator + format_time.substring(0, 4);
				Map<String, Object> modCheckIn = new HashMap<String, Object>();
				
				CheckInFiles = drawMngDAO.selectCheckInModfiles(chkout_path);
				for (int k=0; k<CheckInFiles.size(); k++){
					Map<String, Object> updateModfiles = new HashMap<String, Object>();
					Map<String, Object> getMap = (Map)CheckInFiles.get(k);
					String rfilename = (String)getMap.get("rfilename");
					String fname = rfilename.substring(0, rfilename.lastIndexOf("."));
					String type = rfilename.substring(rfilename.lastIndexOf("."));
					
					if (type.equals(".CATProduct") || type.equals(".CATPart")){
						String modoid = (String)getMap.get("modoid");
						String oid = (String)getMap.get("oid");
						String version = (String)getMap.get("version");
						String fname_hash = FileRenameHash(rfilename, type);
						long fname_size = copyFile(thumb_dir, rfilename, uploadDir+path, fname_hash);
						
						updateModfiles.put("filepath", path);
						updateModfiles.put("version", String.valueOf(Integer.parseInt(version)+1));
						updateModfiles.put("rfilename", rfilename);
						updateModfiles.put("filename", fname_hash);
						updateModfiles.put("filesize", fname_size);
						updateModfiles.put("chkoutpath", chkout_path);
						updateModfiles.put("oid", oid);
						drawMngDAO.updateModfilesCheckIn(updateModfiles);
						
						modCheckIn.put("modoid", modoid);
						modCheckIn.put("staoid", staoid);
						drawMngDAO.updateModCheckIn(modCheckIn);
						
						/* .jpg, .CATDrawing, .pdf, .wrl 업데이트 or 신규등록 */
						for (int a=0; a<arrType.length; a++) {
							Map<String, Object> getMap2 = new HashMap<String,Object>();
							String rfilename2 = "";
							boolean flag = false;
							for (int i=0; i<CheckInFiles.size(); i++){
								getMap2 = (Map)CheckInFiles.get(i);
								rfilename2 = (String)getMap2.get("rfilename");							
								if (rfilename2.contains(fname) && rfilename2.contains(arrType[a])){
									flag = true;
									break;
								}
							}
							
							String fname2 = fname + arrType[a];
							String filepath2 = thumb_dir + File.separator + fname2;
							File f2 = new File(filepath2);
							if(flag){	// 체크아웃 되있으므로 update
								Map<String,Object> tmpModfiles = new HashMap<String,Object>();
								String modoid2 = (String)getMap2.get("modoid");
								String oid2 = (String)getMap2.get("oid");
								String version2 = (String)getMap2.get("version");
								String fname_hash2 = "";
								long fname_size2 = 0;
								if(f2.exists()){			//  aa.CATPart.jpg 형태 때문에 만들어줌
									fname_hash2 = FileRenameHash(fname2, arrType[a]);
									fname_size2 = copyFile(thumb_dir, fname2, uploadDir+path, fname_hash2);
								}else{
									fname_hash2 = FileRenameHash(rfilename2, arrType[a]);
									fname_size2 = copyFile(thumb_dir, rfilename2, uploadDir+path, fname_hash2);
								}
								tmpModfiles.put("filepath", path);
								tmpModfiles.put("version", String.valueOf(Integer.parseInt(version2)+1));
								tmpModfiles.put("rfilename", fname2);				// 여기서 aa.jpg 형태로 바꿔줌 (만약 aa.CATPart.jpg 였다면)
								tmpModfiles.put("filename", fname_hash2);
								tmpModfiles.put("filesize", fname_size2);
								tmpModfiles.put("chkoutpath", chkout_path);
								tmpModfiles.put("oid", oid2);
								drawMngDAO.updateModfilesCheckIn(tmpModfiles);
							}else{		// 등록 안되어있으므로 신규등록 insert
								if(f2.exists()){
									Map<String,Object> tmpModfiles = new HashMap<String,Object>();
									String fileoid2 = drawFileIdgenService.getNextStringId();
									String fname_hash2 = FileRenameHash(fname2, arrType[a]);
									long fname_size2 = copyFile(thumb_dir, fname2, uploadDir+path, fname_hash2);
									String indexno = drawMngDAO.selectMaxIndexNo(modoid);
									
									tmpModfiles.put("oid", fileoid2);
									tmpModfiles.put("modoid", modoid);
									tmpModfiles.put("version", "0");
									tmpModfiles.put("filename", fname_hash2);
									tmpModfiles.put("rfilename", fname2);
									tmpModfiles.put("filesize", fname_size2);
									tmpModfiles.put("masterflag", "F");
									tmpModfiles.put("humid", humid);
									tmpModfiles.put("filepath", path);
									tmpModfiles.put("indexno", String.valueOf(Integer.parseInt(indexno)+1));
									drawMngDAO.insertModfiles2(tmpModfiles);
								}
							}
						}
					}
				}
				
				// 추가파일  파일 이동 및 DB insert ( MOD, MODFILES )
				System.out.println("------------------------- (New) Modfiles & Mod insert ----------------------------");
				for (int k=0; k<tmpModData.size(); k++){
					Map<String, Object> insertMod = new HashMap<String, Object>();
					Map<String,Object> insertModFiles = new HashMap<String,Object>();
					
					Map<String, Object> getMap = (Map)tmpModData.get(k);
					String fname1 = (String)getMap.get("filename");
					String front_fname = fname1.substring(0, fname1.lastIndexOf("."));
					String type = fname1.substring(fname1.lastIndexOf("."));
					String fname1_hash = FileRenameHash(fname1, type);
					long fname1_size = copyFile(thumb_dir, fname1, uploadDir+path, fname1_hash);
					
					/* mod */
					String modoid = drawIdgenService.getNextStringId();
					insertMod.put("oid", modoid);
					insertMod.put("caroid", getMap.get("caroid"));
					insertMod.put("prttypeoid", getMap.get("prttypeoid"));
					insertMod.put("dno", getMap.get("dno"));
					insertMod.put("mversion", getMap.get("mversion"));
					insertMod.put("moduletype", getMap.get("moduletype"));
					insertMod.put("modtypeoid", getMap.get("modtypeoid"));
					insertMod.put("eono", getMap.get("eono"));
					insertMod.put("dscoid", getMap.get("dscoid"));
					insertMod.put("modsizeoid", getMap.get("modsizeoid"));
					insertMod.put("devstep", getMap.get("devstep"));
					insertMod.put("staoid", staoid);
					insertMod.put("humid", humid);
					insertMod.put("dnam", getMap.get("dnam"));
					drawMngDAO.insertOnlyOneMod(insertMod);
					drawrel += modoid + ";" + getMap.get("verprt") + "|"; 	// drawrel 테이블 insert 위함
					File_Oid.put(fname1, modoid);							// 신규 ebom File_Oid [파일명 : modoid]
					
					/* modfiles - 도면 */
	    			String fileoid = drawFileIdgenService.getNextStringId();
	    			insertModFiles.put("oid", fileoid);
	    			insertModFiles.put("modoid", modoid);
	    			insertModFiles.put("version", "0");
	    			insertModFiles.put("filename", fname1_hash);
	    			insertModFiles.put("rfilename", fname1);
	    			insertModFiles.put("filesize", fname1_size);
	    			insertModFiles.put("masterflag", "T");
	    			insertModFiles.put("humid", humid);
	    			insertModFiles.put("filepath", path);
	    			insertModFiles.put("indexno", 1);
					drawMngDAO.insertModfiles2(insertModFiles);
					
					/* .jpg, .CATDrawing, .pdf, .wrl 신규등록 (modfiles 테이블) */
					for (int a=0; a<arrType.length; a++) {
						String fname2 = front_fname + arrType[a];
						String filepath2 = thumb_dir + File.separator + fname2;
						File f2 = new File(filepath2);
						if(f2.exists()){
							Map<String,Object> tmpModfiles = new HashMap<String,Object>();
							String fileoid2 = drawFileIdgenService.getNextStringId();
							String fname2_hash = FileRenameHash(fname2, arrType[a]);
							long fname2_size = copyFile(thumb_dir, fname2, uploadDir+path, fname2_hash);
							String indexno = drawMngDAO.selectMaxIndexNo(modoid);
							
							tmpModfiles.put("oid", fileoid2);
							tmpModfiles.put("modoid", modoid);
							tmpModfiles.put("version", "0");
							tmpModfiles.put("filename", fname2_hash);
							tmpModfiles.put("rfilename", fname2);
							tmpModfiles.put("filesize", fname2_size);
							tmpModfiles.put("masterflag", "F");
							tmpModfiles.put("humid", humid);
							tmpModfiles.put("filepath", path);
							tmpModfiles.put("indexno", String.valueOf(Integer.parseInt(indexno)+1));
							drawMngDAO.insertModfiles2(tmpModfiles);
						}
					}
				}
				
				System.out.println("------------------------- EBOM start ----------------------------");
				List<Map<String, Object>> tmpfileoid = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> delEbomList = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> retEbom = null;
				
				// ebom 구조
				HashMap<String,Object> modInfo = new HashMap<String,Object>();
				modInfo.put("modoid", chkRootOid);
				List<Map<String, Object>> ebomInfo = drawMngDAO.selectEbomTreeList(modInfo);
				// ebom 의 modoid로 modfiles 정보 가져오기
				List<String> oidList = new ArrayList<String>();
				for (int i=0; i<ebomInfo.size(); i++){
					Map tempMap = (Map)ebomInfo.get(i);
					oidList.add((String)tempMap.get("modoid"));
				}
				tmpfileoid = drawMngDAO.selectOriEbomFileOid(oidList);
				for (int i=0; i<tmpfileoid.size(); i++){
					String rfile = (String)tmpfileoid.get(i).get("rfilename");
					String moid = (String)tmpfileoid.get(i).get("modoid");
					File_Oid.put(rfile, moid);
				}
				// EBOM 기존꺼 제거
				if(ebomInfo != null && EBOMList != null){
		   			for(int i=0; i < ebomInfo.size(); i++){
		   				Map ebomMap = (Map)ebomInfo.get(i);
		   				String parentoid = (String)ebomMap.get("parentoid");
		   				if(parentoid != null){
			   				Map<String, Object> putMap = new HashMap<String, Object>();
							putMap.put("modoid", (String)ebomMap.get("modoid"));
							putMap.put("parentoid", parentoid);
							putMap.put("seq", String.valueOf(ebomMap.get("seq")));
							delEbomList.add(putMap);
		   				}
		   			}
		   		}
				// 기존 ebom 삭제
				retEbom = new ArrayList<Map<String, Object>>(new HashSet<Map<String, Object>>(delEbomList));
		   		
		   		for(int k=0; k < retEbom.size(); k++){
		   			Map<String, Object> tempMap = new HashMap<String, Object>();
		   			tempMap = (Map)retEbom.get(k);
		   			drawMngDAO.deleteEbomList(tempMap);
		   		}
		   		// ebom 생성
		   		List<Map<String,Object>> insertEbom = new ArrayList<Map<String,Object>>();
				List<Map<String, Object>> resEBOM = null;
				for (int idx=0; idx<EBOMList.size(); idx++){
					Map<String, Object> ebomMap = new HashMap<String, Object>();
					String curFile = (String)EBOMList.get(idx).get("cur_filename");
					String parFile = (String)EBOMList.get(idx).get("par_filename");
					String curOid = "";
					String parOid = "";
					
					if (File_Oid.containsKey(curFile) == true) {
						curOid = (String)File_Oid.get(curFile);
					}
					if (File_Oid.containsKey(parFile) == true) {
						parOid = (String)File_Oid.get(parFile);
					}
					
					if (curOid != null && !curOid.equals("") && parOid != null && !parOid.equals("")){
						System.out.println(curOid + " : " + parOid);
						ebomMap.put("modoid", curOid);
						ebomMap.put("parentoid", parOid);
						ebomMap.put("seq", EBOMList.get(idx).get("seq"));
						ebomMap.put("humid", humid);
						ebomMap.put("lastmodoid", curOid);
						insertEbom.add(ebomMap);
					}
				}
				// 중복 제거	
				resEBOM = new ArrayList<Map<String, Object>>(new HashSet<Map<String, Object>>(insertEbom));
				if(resEBOM.size()>0){
					drawMngDAO.insertNewRegistEbom(resEBOM);
				}
				
				// DB insert (DRAWREL)
				System.out.println("--------------------------- drawrel insert ---------------------------");
				String[] arrDatas2 = drawrel.split("\\|");
				for(int i=0; i<arrDatas2.length; i++){
					String[] tempData = arrDatas2[i].split(";");
					Map<String, Object> tempMap = new HashMap<String, Object>();
					String modoid = tempData[0];
					String reloid = drawMngDAO.selectMaxVerPno(tempData[1]);
					tempMap.put("modoid", modoid);
					tempMap.put("reloid", reloid);
					tempMap.put("ref", "F");
					drawMngDAO.checkoutRevisionDrawRel(tempMap);
				}
		   		
				msg = "Success";
			}catch(Exception e){
				e.printStackTrace();
			}
			return msg;
		}
		
		/* 신규등록 */
		public String insertNewRegistEBOM(Map<String, Object> map) throws Exception{
			String msg = "fail";
			HashMap<String, Object> File_Oid = new HashMap<String, Object>();
			
			try{
				String staoid = (String)map.get("put_staoid");
				String moddata = (String)map.get("moddata");
				String thumb_dir = (String)map.get("thumb_dir");
				String humid = (String)map.get("id");
				String drawrel = "";
				String[] arrType = {".jpg", ".CATDrawing", ".pdf", ".wrl"};
				
				List<Map<String, Object>> EBOMList = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> LinkIDList = new ArrayList<Map<String, Object>>();
				EBOMList = (List<Map<String, Object>>)map.get("EBOMList");
				LinkIDList = (List<Map<String, Object>>)map.get("LinkIDList");
				
				String[] arrDatas = moddata.split("\\|");
				List<Map<String,Object>> tmpModData = new ArrayList<Map<String,Object>>();
				for(int i=0; i<arrDatas.length; i++){
					String[] tempData = arrDatas[i].split(";");
					Map<String, Object> tempMap = new HashMap<String, Object>();
					tempMap.put("filename", tempData[0]);
					tempMap.put("caroid", tempData[1]);
					tempMap.put("prttypeoid", tempData[2]);
					tempMap.put("dno", tempData[3]);
					tempMap.put("mversion", tempData[4]);
					tempMap.put("moduletype", tempData[5]);
					tempMap.put("modtypeoid", tempData[6]);
					tempMap.put("eono", tempData[7]);
					tempMap.put("dscoid", tempData[8]);
					tempMap.put("modsizeoid", tempData[9]);
					tempMap.put("devstep", tempData[10]);
					tempMap.put("verprt", tempData[11]);
					tempMap.put("dnam", tempData[12]);
					tmpModData.add(tempMap);
				}
				
				SimpleDateFormat format = new SimpleDateFormat ("yyyyMMddHHmmss");
				String format_time = format.format (System.currentTimeMillis());
				String path = File.separator + format_time.substring(0, 4);
				
				// 파일 이동 및 DB insert ( MOD, MODFILES )
				System.out.println("------------------------- Modfiles & Mod insert ----------------------------");
				for (int k=0; k<tmpModData.size(); k++){
					Map<String, Object> insertMod = new HashMap<String, Object>();
					Map<String,Object> insertModFiles = new HashMap<String,Object>();
					
					Map<String, Object> getMap = (Map)tmpModData.get(k);
					String fname1 = (String)getMap.get("filename");
					String front_fname = fname1.substring(0, fname1.lastIndexOf("."));
					String type = fname1.substring(fname1.lastIndexOf("."));
					String fname1_hash = FileRenameHash(fname1, type);
					long fname1_size = copyFile(thumb_dir, fname1, uploadDir+path, fname1_hash);
					
					/* mod */
					String modoid = drawIdgenService.getNextStringId();
					insertMod.put("oid", modoid);
					insertMod.put("caroid", getMap.get("caroid"));
					insertMod.put("prttypeoid", getMap.get("prttypeoid"));
					insertMod.put("dno", getMap.get("dno"));
					insertMod.put("mversion", getMap.get("mversion"));
					insertMod.put("moduletype", getMap.get("moduletype"));
					insertMod.put("modtypeoid", getMap.get("modtypeoid"));
					insertMod.put("eono", getMap.get("eono"));
					insertMod.put("dscoid", getMap.get("dscoid"));
					insertMod.put("modsizeoid", getMap.get("modsizeoid"));
					insertMod.put("devstep", getMap.get("devstep"));
					insertMod.put("staoid", staoid);
					insertMod.put("humid", humid);
					insertMod.put("dnam", getMap.get("dnam"));
					drawMngDAO.insertOnlyOneMod(insertMod);
					drawrel += modoid + ";" + getMap.get("verprt") + "|"; 	// drawrel 테이블 insert 위함
					File_Oid.put(fname1, modoid);							// 신규 ebom File_Oid [파일명 : modoid]
					
					/* modfiles - 도면 */
	    			String fileoid = drawFileIdgenService.getNextStringId();
	    			insertModFiles.put("oid", fileoid);
	    			insertModFiles.put("modoid", modoid);
	    			insertModFiles.put("version", "0");
	    			insertModFiles.put("filename", fname1_hash);
	    			insertModFiles.put("rfilename", fname1);
	    			insertModFiles.put("filesize", fname1_size);
	    			insertModFiles.put("masterflag", "T");
	    			insertModFiles.put("humid", humid);
	    			insertModFiles.put("filepath", path);
	    			insertModFiles.put("indexno", 1);
					drawMngDAO.insertModfiles2(insertModFiles);
					
					/* .jpg, .CATDrawing, .pdf, .wrl 신규등록 (modfiles 테이블) */
					for (int a=0; a<arrType.length; a++) {
						String fname2 = front_fname + arrType[a];
						String filepath2 = thumb_dir + File.separator + fname2;
						File f2 = new File(filepath2);
						if(f2.exists()){
							Map<String,Object> tmpModfiles = new HashMap<String,Object>();
							String fileoid2 = drawFileIdgenService.getNextStringId();
							String fname2_hash = FileRenameHash(fname2, arrType[a]);
							long fname2_size = copyFile(thumb_dir, fname2, uploadDir+path, fname2_hash);
							String indexno = drawMngDAO.selectMaxIndexNo(modoid);
							
							tmpModfiles.put("oid", fileoid2);
							tmpModfiles.put("modoid", modoid);
							tmpModfiles.put("version", "0");
							tmpModfiles.put("filename", fname2_hash);
							tmpModfiles.put("rfilename", fname2);
							tmpModfiles.put("filesize", fname2_size);
							tmpModfiles.put("masterflag", "F");
							tmpModfiles.put("humid", humid);
							tmpModfiles.put("filepath", path);
							tmpModfiles.put("indexno", String.valueOf(Integer.parseInt(indexno)+1));
							drawMngDAO.insertModfiles2(tmpModfiles);
						}
					}
				}
				
				System.out.println("------------------------- EBOM start ----------------------------");
		   		// ebom 생성
		   		List<Map<String,Object>> insertEbom = new ArrayList<Map<String,Object>>();
				List<Map<String, Object>> resEBOM = null;
				for (int idx=0; idx<EBOMList.size(); idx++){
					Map<String, Object> ebomMap = new HashMap<String, Object>();
					String curFile = (String)EBOMList.get(idx).get("cur_filename");
					String parFile = (String)EBOMList.get(idx).get("par_filename");
					String curOid = "";
					String parOid = "";
					
					if (File_Oid.containsKey(curFile) == true) {
						curOid = (String)File_Oid.get(curFile);
					}
					if (File_Oid.containsKey(parFile) == true) {
						parOid = (String)File_Oid.get(parFile);
					}
					
					if (curOid != null && !curOid.equals("") && parOid != null && !parOid.equals("")){
						System.out.println(curOid + " : " + parOid);
						ebomMap.put("modoid", curOid);
						ebomMap.put("parentoid", parOid);
						ebomMap.put("seq", EBOMList.get(idx).get("seq"));
						ebomMap.put("humid", humid);
						ebomMap.put("lastmodoid", curOid);
						insertEbom.add(ebomMap);
					}
				}
				// 중복 제거	
				resEBOM = new ArrayList<Map<String, Object>>(new HashSet<Map<String, Object>>(insertEbom));
				if(resEBOM.size()>0){
					drawMngDAO.insertNewRegistEbom(resEBOM);
				}
				
				// DB insert (DRAWREL)
				System.out.println("--------------------------- drawrel insert ---------------------------");
				String[] arrDatas2 = drawrel.split("\\|");
				for(int i=0; i<arrDatas2.length; i++){
					String[] tempData = arrDatas2[i].split(";");
					Map<String, Object> tempMap = new HashMap<String, Object>();
					String modoid = tempData[0];
					String reloid = drawMngDAO.selectMaxVerPno(tempData[1]);
					tempMap.put("modoid", modoid);
					tempMap.put("reloid", reloid);
					tempMap.put("ref", "F");
					drawMngDAO.checkoutRevisionDrawRel(tempMap);
				}
		   		
				msg = "Success";
			}catch(Exception e){
				e.printStackTrace();
			}
			return msg;
		}
		
		public List<Map<String, Object>> selectPnoMaxStaoid(List<String> arr) throws Exception {
			return drawMngDAO.selectPnoMaxStaoid(arr);
		}
		
		public List<Map<String, Object>> selectLatestEbomParent(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectMainParentLastoid(map);
		}
		
		public List<Map<String, Object>> selectLatestEbomChild(Map<String, Object> map) throws Exception {
			return drawMngDAO.selectLatestEbomChild(map);
		}
		
		public List<Map<String, Object>> selectMainSearchModfiles2(List<String> arr) throws Exception {
			return drawMngDAO.selectMainSearchModfiles2(arr);
		}
		
		public List<Map<String, Object>> selectMainSearchModfilehistory2(List<String> arr) throws Exception {
			return drawMngDAO.selectMainSearchModfilehistory2(arr);
		}
		
		public List<Map<String, Object>> selectLatestModInfo(List<String> arr) throws Exception {
			return drawMngDAO.selectLatestModInfo(arr);
		}
		
		public List<Map<String, Object>> testhayan(Map<String, Object> map) throws Exception {
			/* EBOM 테이블 개정  */
			List<Map<String,Object>> insertEbom = new ArrayList<Map<String,Object>>();
			List<Map<String, Object>> resEBOM = null;
			List<Map<String, Object>> getOriEbom = drawMngDAO.selectLatestEbomChild(map);
			
			// 가져온 getOriEbom 최신버전으로 가공
			HashMap<String, Object> cur_last_oid = new HashMap<String, Object>();
			for (int i=0; i<getOriEbom.size(); i++) {
				Map<String, Object> getMap = (Map)getOriEbom.get(i);
				if (getMap.get("lastmodoid") != null && !getMap.get("lastmodoid").equals(getMap.get("modoid"))){
					String cur_modoid = (String)getMap.get("modoid");
					String last_modoid = (String)getMap.get("lastmodoid");
					cur_last_oid.put(cur_modoid, last_modoid);
				}
			}
			for (Map<String, Object> row : getOriEbom) {
				boolean chgFlag = false;
				if (row.get("parentoid") != null && !row.get("parentoid").equals("")) {
					String modoid = (String)row.get("modoid");
					String parentoid = (String)row.get("parentoid");
					if (cur_last_oid.containsKey(modoid) == true) {
						modoid = (String)cur_last_oid.get(modoid);
						chgFlag = true;
					}
					if (cur_last_oid.containsKey(parentoid) == true) {
						parentoid = (String)cur_last_oid.get(parentoid);
						chgFlag = true;
					}
					if (chgFlag) {
						row.put("modoid", modoid);
						row.put("parentoid", parentoid);
					}
				}
			}// 가공끝
			
			/*List<Map<String, Object>> getOriEbom = drawMngDAO.selectgetOriEbom("MOD00006096");
			List<Map<String, Object>> tempList = new ArrayList<Map<String,Object>>();
			List<Map<String, Object>> resEBOM = null;
			
			for (int i=0; i<getOriEbom.size(); i++) {
				String parentoid = (String)getOriEbom.get(i).get("parentoid");
				if (parentoid != null && !parentoid.equals("")) {
					Map<String, Object> tempMap = new HashMap<String, Object>();				
					tempMap.put("modoid", getOriEbom.get(i).get("modoid"));
					tempMap.put("parentoid", parentoid);
					tempMap.put("seq", getOriEbom.get(i).get("seq"));
					tempList.add(tempMap);
				}
			}
			
			resEBOM = new ArrayList<Map<String, Object>>(new HashSet<Map<String, Object>>(tempList));
			
			System.out.println(getOriEbom.size());
			System.out.println(resEBOM.size());*/
			
			for (int i=0; i<getOriEbom.size(); i++) {
				Map<String, Object> tempMap = getOriEbom.get(i);
				System.out.println(tempMap.get("modoid") + " " + tempMap.get("parentoid") + " " + tempMap.get("seq"));
			}
			
			return getOriEbom;
		}
		
		public String selectModInfo(Map<String, Object> map) throws Exception{
			return drawMngDAO.selectModInfo(map);
		}
		
		public String selectMaxModoid(Map<String, Object> map) throws Exception{
			return drawMngDAO.selectMaxModoid(map);
		}
		
	}
