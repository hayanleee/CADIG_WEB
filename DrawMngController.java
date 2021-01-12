package com.yura.draw.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Resource;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.vfs2.FileNotFoundException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.yura.common.util.FileUploadUtil;
import com.yura.common.util.FileVO;
import com.yura.draw.DrawVO;
import com.yura.draw.service.DrawMngService;
import com.yura.draw.service.impl.DrawBOMExcelCreate;
import com.yura.draw.util.Draw3DFileProcess;
import com.yura.draw.util.EBOMTree;
import com.yura.draw.util.YuraException;
import com.yura.part.service.PartMngService;
import com.yura.part.service.impl.PartBOMExcelCreate;
import com.yura.prj.service.PrjService;
import com.yura.user.service.UserMngService;

import egovframework.com.cmm.EgovMessageSource;
import egovframework.com.cmm.EgovWebUtil;
import egovframework.com.cmm.LoginVO;
import egovframework.com.cmm.service.EgovProperties;
import egovframework.com.cmm.util.EgovBasicLogger;
import egovframework.com.cmm.util.EgovResourceCloseHelper;
import egovframework.com.cmm.util.EgovUserDetailsHelper;
import egovframework.com.cop.ems.service.EgovSndngMailRegistService;
import egovframework.com.cop.ems.service.SndngMailVO;
import egovframework.com.uat.uia.service.EgovLoginService;
import egovframework.com.utl.fcc.service.EgovFormBasedFileUtil;
import egovframework.com.utl.sim.service.EgovFileScrty;
import egovframework.com.utl.sim.service.EgovFileTool;
import egovframework.rte.fdl.idgnr.EgovIdGnrService;

@Controller
public class DrawMngController implements ApplicationContextAware,
		InitializingBean {

	private ApplicationContext applicationContext;

	private static final int BUFF_SIZE = 1024 * 1024 * 100;

	@Resource(name="egovMessageSource")
    EgovMessageSource egovMessageSource;

	/** egovDrawManageIdGnrService */
    @Resource(name="egovDrawManageIdGnrService")
    private EgovIdGnrService drawIdgenService;

    @Resource(name="egovDrawFileManageIdGnrService")
    private EgovIdGnrService drawFileIdgenService;
    
	/** egovDrawManageIdGnrService */
    @Resource(name="egovDrawDnoIdGnrService")
    private EgovIdGnrService drawDnoIdgenService;
    
    /** EgovLoginService */
	@Resource(name = "loginService")
	private EgovLoginService loginService;
	
	@Resource(name = "prjService")
	private PrjService prjService;
	
	private FileUploadUtil fileUtil;
	
    /** EgovSndngMailRegistService */
	@Resource(name = "sndngMailRegistService")
    private EgovSndngMailRegistService sndngMailRegistService;

	@Resource(name = "userMngService")
	private UserMngService userMngService;
    
	@Resource(name = "distcomOidGnrService")
    private EgovIdGnrService distcomOidGnrService;	
	
    /** 첨부파일 위치 지정 */
    private final String uploadDir = EgovProperties.getProperty("Globals.fileStorePath.draw");

    /** 첨부파일 Temp폴더 위치 지정 */
    private final String uploadDirTemp = EgovProperties.getProperty("Globals.fileStorePath.draw.temp");
	
    /** CAD Check 폴더 위치 지정 */
    private final String uploadDirCad = EgovProperties.getProperty("Globals.fileStorePath.draw.cad");

    /** yPLM 시스템 URL */
    private final String systemURL = EgovProperties.getProperty("Globals.Server.Url");

    /** 웹 3d 뷰어 임시 폴더 */
    private final String vizwTmpDir = EgovProperties.getProperty("Globals.fileStorePath.draw.viz.tmp");
    
    /** 웹 3d 뷰어 폴더 */
    private final String vizwDir = EgovProperties.getProperty("Globals.fileStorePath.draw.viz.vizw");
    
    /** 첨부 최대 파일 크기 지정 */
    private final long maxFileSize = 1024 * 1024 * 100;   //업로드 최대 사이즈 설정 (100M)
    
	private static final Logger LOGGER = LoggerFactory.getLogger(DrawMngController.class);

	public void afterPropertiesSet() throws Exception {}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		
		LOGGER.info("PartMngController setApplicationContext method has called!");
	}

	@Resource(name ="DrawMngService")
    private DrawMngService drawMngService;

	@Resource(name = "PartMngService")
    private PartMngService partMngService;
	
    /**
     * 도면등록 화면으로으로 이동한다.
     *
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping("/yura/mod/modRegistration.do")
    public String drawRegister(HttpServletRequest request, ModelMap model) throws Exception {
    	return "yura/draw/insert/drawRegister";
    }
    
    /**
     * 도면배포 진행현황 화면으로 이동한다.
     *
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping("/yura/mod/modStatus.do")
    public String modStatus(HttpServletRequest request, ModelMap model) throws Exception {
    	
    	return "yura/draw/select/modStatus";
    }
    
    /**
     * 도면 BOM 비교 화면으로 이동한다.
     *
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping("/yura/mod/modCompareBomInformation.do")
    public String modCompareBomInformation(HttpServletRequest request, ModelMap model) throws Exception {
    	
    	return "yura/draw/select/modCompareBomInformation";
    }  
    
    
    /**
	 * 도면정보 등록
	 *
	 * @param 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/draw/insert/registerDrawInfo.do", method=RequestMethod.POST)
	public String registerDrawInfo(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, 
			MultipartHttpServletRequest multiRequest, ModelMap model) throws Exception{
		
		LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
		HashMap<String, Object> map = new HashMap<String, Object>();
		String filePath = "";
		String dno = drawDnoIdgenService.getNextStringId();
		/*
		 * 도면기본정보 
		 */
		map.put("oidList", 		(String)commandMap.get("oidList"));	
		map.put("modsizeoid", 	(String)commandMap.get("modsizeoid"));
		map.put("modtypeoid", 	(String)commandMap.get("modtypeoid"));
		map.put("moduletype", 	(String)commandMap.get("moduletype"));
		map.put("eono", 		(String)commandMap.get("eono"));
		map.put("engctgoid", 	(String)commandMap.get("engctgoid"));
		map.put("prttypeoid", 	(String)commandMap.get("prttypeoid"));
		//map.put("dno", 			(String)commandMap.get("dno"));
		map.put("dno", 			dno);
		map.put("dnam", 	(String)commandMap.get("dnam"));
		map.put("mversion", 	(String)commandMap.get("mversion"));
		map.put("reloid", 		(String)commandMap.get("reloid"));
		map.put("dscoid", 		(String)commandMap.get("dscoid"));
		map.put("devstep", 		(String)commandMap.get("devstep"));
		map.put("masterFilename", (String)commandMap.get("masterFilename"));
		map.put("disthumid", (String)commandMap.get("disthumid"));
		map.put("distteams", (String)commandMap.get("distteams"));
		map.put("distcoms", (String)commandMap.get("distcoms"));
		map.put("relmodule", (String)commandMap.get("relmodule"));
		String description = URLDecoder.decode((String)commandMap.get("description"), "UTF-8");
		map.put("description", description);
		map.put("humid", 		loginVO.getId());
		
		String subasmcheck = (String)commandMap.get("subasmcheck");
		map.put("subasmcheck",  subasmcheck);
		
		/*
		 * 도면배포용 정보
		 */
		/*
		map.put("title", 	(String)commandMap.get("title"));
		map.put("content", 	(String)commandMap.get("content"));
		map.put("engene", 	(String)commandMap.get("engene"));
		map.put("pno", 		(String)commandMap.get("pno"));
		map.put("disthumid",(String)commandMap.get("disthumid"));
		map.put("distteams", (String)commandMap.get("distteams"));
		map.put("distcoms", (String)commandMap.get("distcoms"));
		map.put("distmods", (String)commandMap.get("distmods"));
		map.put("distdraws",  commandMap.get("distdraws"));
		map.put("partname", (String)commandMap.get("partname"));
		map.put("acceptdate", (String)commandMap.get("acceptdate"));
		map.put("ecodate", (String)commandMap.get("ecodate"));
		map.put("reghumname",loginVO.getId());
		*/
		
		
		//filePath = uploadDir + (String)map.get("dno") + "_" +(String)map.get("mversion");
	
		String tmpfilename = (String)multiRequest.getParameter("tmpfilename");
		String filename = (String)multiRequest.getParameter("orifilename");
		String filesize = (String)multiRequest.getParameter("orifilesize");
		String[] tmpFilename = tmpfilename.split(",");
		String[] arrFilename = filename.split(",");
		String[] arrFilesize = filesize.split(",");
		
		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat ("yyyy", Locale.KOREA);
		Date cDate = new Date();
		String currentYear = mSimpleDateFormat.format ( cDate );
		String relFilePath = "";
		relFilePath = uploadDir+currentYear+File.separator;
		List<FileVO> fileList = new ArrayList<FileVO>();
		try{
			if (arrFilename.length > 0) {
				EgovFileTool.deleteDirectory(filePath);
				for(int i=0; i<arrFilename.length;i++){
					FileVO fileVO = new FileVO();
					fileVO.setPhysicalName(tmpFilename[i]);
					fileVO.setFileName(arrFilename[i]);
					fileVO.setSize(Long.parseLong(arrFilesize[i]));
					fileVO.setExtName(arrFilename[i].substring(arrFilename[i].lastIndexOf(".")+1, arrFilename[i].length()));
					fileVO.setFilePath(currentYear);
					//Temp폴더에서 실제폴더로 파일 복사
					//copyFile2(tmpFilename[i], arrFilename[i], uploadDirTemp, uploadDir);
					copyFile2(tmpFilename[i], tmpFilename[i], uploadDirTemp, relFilePath);
					
					fileList.add(fileVO);
				}
			}
		
			Map<String, Object> result = drawMngService.registertDrawInfo(fileList, map);
			
			//사용이력 등록
			Map<String, Object>vo = new HashMap<String, Object>();
			vo.put("refoid", result.get("modoid"));
			vo.put("userid", loginVO.getId());
			vo.put("usetype", "C");
			vo.put("usedes", "");
			userMngService.insertUseHistory(vo);

			
			model.addAttribute("oidList", result.get("oidList"));		
			model.addAttribute("oid", result.get("modoid"));		
			model.addAttribute("resultMsg", egovMessageSource.getMessage("success.common.insert"));		
			model.addAttribute("result",  "SUCCESS");	
			
		} catch (Exception e) {
			for(int i=0; i<fileList.size(); i++)
				EgovFileTool.deleteFile(uploadDirTemp + fileList.get(i).getPhysicalName());	
			EgovFileTool.deleteDirectory(filePath);
			LOGGER.error(e.getMessage(), e);
			System.out.println("도면정보 등록 에러");
			model.addAttribute("oid",  "");
			model.addAttribute("resultMsg",  egovMessageSource.getMessage("fail.common.insert"));
			model.addAttribute("result",  "FAIL");
		}finally{
			for(int i=0; i<fileList.size(); i++)
				EgovFileTool.deleteFile(uploadDirTemp + fileList.get(i).getPhysicalName());	
		}
		
		return "yura/draw/insert/registerDrawInfo";
	}
	
	/**
     * 도면OID 반환
     *
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping("/draw/select/getDrawOid.do")
    public String getDrawOid(ModelMap model) throws Exception {
    	
    	String oid = drawIdgenService.getNextStringId();
    	model.addAttribute("result", oid);
    	return "yura/part/select/jsonResultData";
    }
    
    /**
     * 도면첨부파일 업로드
     */
   @RequestMapping(value="/draw/insert/insertFileUpload.do", method=RequestMethod.POST)
    public Object insertFileUpload(final HttpServletRequest request, ModelMap model
	    		,final MultipartHttpServletRequest multiRequest, DrawVO vo
	    ) throws Exception  {
	    	
	   String pno = request.getParameter("pno");
	   String verprtoid = request.getParameter("verprtoid");
	   String modkindoid = request.getParameter("modkindoid");
	   String subasmcheck = request.getParameter("subasmcheck");
	   String pvsname = request.getParameter("pvsname");
	   List<FileVO> list = null;
	   
	   HashMap<String, Object> map = new HashMap<String, Object>();
	   map.put("verprtoid", verprtoid);
	   map.put("modkindoid", modkindoid);
	   Object dirseq = drawMngService.retrieveDirSeq(map);
	   /*
	    * 2D일경우
	    */
//	   if(modkindoid.equals("CCN00064")){
	   if(subasmcheck == null || subasmcheck.equals("F")){
		   list = fileUtil.uploadFiles2(request, uploadDir, maxFileSize);
	   }else{
		   String filePath = uploadDir+ "/"+pno+"_"+dirseq+"_"+pvsname;
		   vo.setFilepath(filePath);
		   list = fileUtil.uploadFiles3D(request, filePath, maxFileSize);   
	   }
		List<String> oidList = new ArrayList<String>();
		
		if (list.size() > 0) {
			oidList = drawMngService.registerAttachFile(list, vo);
		}
		model.addAttribute("oidList", oidList);
		return "yura/draw/insert/insertFileUpload";
    }
   
	/**
	 * 도면 등록 전 파일업로드
	 */
 @RequestMapping(value="/draw/insert/registerDrawFileInfo.do")
 public String registerDrawFileInfo(final MultipartHttpServletRequest multiRequest, HttpServletRequest request, ModelMap model) throws Exception{
	
	  	List<FileVO> fileList = new ArrayList<FileVO>(); 
	   if(request instanceof MultipartHttpServletRequest) {
	    	try {
	    		fileList = fileUtil.uploadFiles4(request, uploadDirTemp, maxFileSize);   
			}catch(Exception e) {
				System.out.println("파일 업로드 오류 발생");
				model.addAttribute("result", "fail");
			}
		}
		
	   if(fileList!= null && fileList.size()>0){
		   model.addAttribute("result", "success");
		   model.addAttribute("fileList", fileList);
	   }
	   return "/yura/draw/select/registerDrawFileInfo";
 }
   
   
   /**
    * 도면 등록 전 파일삭제
    */
   @RequestMapping(value="/yura/draw/delete/deleteDrawFileInfo.do")
   public String deleteDrawFileInfo(HttpServletRequest request, ModelMap model) throws Exception{
	   
		String result="1";
		try {
			//문서 파일삭제 진행
			result = FileUploadUtil.deleteFile(request, uploadDirTemp, maxFileSize);
				
		}catch(Exception e) {
			System.out.println("파일삭제 오류 발생");
			result = "0";
		}

		model.addAttribute("result", result);
	   return "/yura/draw/select/jsonResultData";
   }

   /**
    * 도면첨부파일 리스트
    */
  @RequestMapping(value="/draw/select/selectModFileList.do", method=RequestMethod.POST)
   public Object selectModFileList(HttpServletRequest request, Model model) throws Exception {
	   
	  	List<Map<String, Object>> result = null;	
	  	String temp = request.getParameter("oidList");
	  	int size = 0;
	  	if(temp != null){
		  	String[] oidList = temp.split("[,]");
			  
			if (oidList.length > 0) {
				result = drawMngService.selectModFileList(oidList);
			}
	  	}
		
		if(result != null) 
			size = result.size();
		
		model.addAttribute("resultCnt", size);
		model.addAttribute("JSONDataList", result);
		return "yura/draw/select/selectModFileList";
   }

   /**
    * checkbox용 dataTables
    */
   @RequestMapping(value="/draw/select/selectDataList.do")
   public String selectDataList(HttpServletRequest request, Model model) throws Exception {
	   
	   return "yura/draw/select/jsonResultList";
   }

   /**
    * 도면버전정보 조회
    */
   @RequestMapping(value="/draw/select/retrievePreDrawList.do", method=RequestMethod.POST)
   public String retrievePreDrawList(@ModelAttribute("drawVO") DrawVO drawVO, ModelMap model) throws Exception {
	   
	   List<Map<String, Object>> checkResult = null;
	   List<Map<String, Object>> result = null;
   		
	   	try {
	   		checkResult = drawMngService.checkPartDrawInfo(drawVO);
	   		if(checkResult.size()>0){
	   			result = drawMngService.retrievePreDrawList(drawVO);
	   		}
	   		model.addAttribute("resultMsg", egovMessageSource.getMessage("success.common.select"));
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("도면버전정보 조회 에러");
			model.addAttribute("resultMsg", egovMessageSource.getMessage("fail.common.select"));
		}
	   	
		model.addAttribute("JSONDataList", result);
	   	return "yura/part/select/jsonResultList";
   }

   /**
    * CAD종류 조회
    *
    * @param iscad
    * @return
    * @throws Exception
    */
   @RequestMapping("/draw/select/selectSftInfo.do")
   public String selectSftInfo(@RequestParam("iscad") String iscad, ModelMap model) throws Exception {
   	
   	List<Map<String, Object>> resultData = null;
   	try {
   		resultData = drawMngService.selectSftInfo(iscad);
   		model.addAttribute("resultMsg", egovMessageSource.getMessage("success.common.select"));
	} catch (Exception e) {
		LOGGER.error(e.getMessage(), e);
		System.out.println("도면개정리스트 조회 에러");
		model.addAttribute("resultMsg", egovMessageSource.getMessage("fail.common.select"));
	}
   	
   	model.addAttribute("JSONDataList", resultData);
   	return "yura/part/select/jsonResultList";
   }
   
   /**
    * 배포담당자 정보 가져오기
    */
	@RequestMapping(value="/draw/select/selecUserListSearching.do")
	public String selecUserListSearching(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("teamoid", (String)commandMap.get("teamoid"));
		map.put("humoid", (String)commandMap.get("humoid"));
		map.put("name", (String)commandMap.get("name"));
		map.put("teamname", (String)commandMap.get("teamname"));
		List<Map<String, Object>>  result = drawMngService.selecUserListSearching(map);
		int resultCnt = drawMngService.selecUserListSearchingCnt(map);
		
		model.addAttribute("JSONDataList", result);
		model.addAttribute("resultCnt", resultCnt);
		
		return "yura/draw/select/selecUserListSearching";
	}	
	
	/**
	 * 서브파트 여부 체크
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> checkSubPart(HashMap<String, String> map) throws Exception{
		return drawMngService.checkSubPart(map);
	}
	
	/**
	 * 도면 개정정보 리스트
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/draw/select/selectPreDrawList.do")
	public String selectPreDrawList(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{

		HashMap<String,String> map = new HashMap<String,String>();
		map.put("verprtoid", (String)commandMap.get("verprtoid"));
		
		List<Map<String, Object>> result = null;
		List<Map<String, Object>> subPart = drawMngService.checkSubPart(map);
		if(subPart != null && subPart.get(0).get("checksubpart").toString().equals("TRUE")){
			result = drawMngService.selectPreDrawList(map);
		}
		
		model.addAttribute("JSONDataList", result);
		model.addAttribute("resultCnt", result.size());
		return "yura/draw/select/selectPreDrawList";
	}
	
	
	/**
	 * 배포담당자 리스트
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/draw/select/userDistSearching.do")
	public String userDistSearching(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("humid", (String)commandMap.get("humid"));
		map.put("humname", (String)commandMap.get("humname"));
		map.put("teamname", (String)commandMap.get("teamname"));
		
		List<Map<String, Object>>  result = drawMngService.userDistSearching(map);
		
		model.addAttribute("JSONDataList", result);
		model.addAttribute("resultCnt", result.size());
		return "yura/draw/select/selectPreDrawList";
	}
	
	
	/**
	 * 협력업체 리스트
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/draw/select/comDistSearching.do")
	public String selectComDistSearching(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("name", (String)commandMap.get("name"));
		map.put("labor", (String)commandMap.get("labor"));
		map.put("laborid", (String)commandMap.get("laborid"));
		map.put("teamcom", (String)commandMap.get("teamcom"));
		map.put("distoid", (String)commandMap.get("distoid"));
		
		List<Map<String, Object>>  result = drawMngService.comDistSearching(map);
		
		model.addAttribute("JSONDataList", result);
		model.addAttribute("resultCnt", result.size());
		return "yura/draw/select/selectPreDrawList";
	}
	
	
	/**
	 * 일반도면/프로젝트도면 미결재 리스트
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/draw/select/retrieveApprovalList.do")
	public String retrieveApprovalList(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    	
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("moduletype", (String)commandMap.get("moduletype"));//CCN00056 : 일반도면일 경우
		map.put("eono", (String)commandMap.get("eono"));
		map.put("pno", (String)commandMap.get("pno"));
		map.put("reloid", (String)commandMap.get("reloid"));
		map.put("userid", loginVO.getId());
		
		List<Map<String, Object>>  result = drawMngService.retrieveApprovalList(map);
		
		model.addAttribute("JSONDataList", result);
		model.addAttribute("resultCnt", result.size());
		return "yura/draw/select/selectPreDrawList";
	}
	
	
	/**
	 * 결재 첨부 문서 리스트
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/draw/select/retrieveDrawDocList.do")
	public String retrieveDrawDocList(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("modoid", (String)commandMap.get("modOid"));
		
		List<Map<String, Object>>  result = drawMngService.retrieveDrawDocList(map);
		
		model.addAttribute("JSONDataList", result);
		model.addAttribute("resultCnt", result.size());
		return "yura/draw/select/selectPreDrawList";
	}
	
	
	/**
	 * 도면개정체크 및 동일개정 도면등록여부 체크
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
/*	@RequestMapping(value="/draw/select/retrieveDrawPvsCheck.do")
	public String retrieveDrawPvsCheck(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("pvsname", (String)commandMap.get("pvsname"));
		map.put("modtypeoid", (String)commandMap.get("modtypeoid"));
		map.put("verprtoid", (String)commandMap.get("verprtoid"));
		map.put("indexno", (String)commandMap.get("indexno"));
		
		Object  checkPvs = drawMngService.drawCheckPvs(map);
		model.addAttribute("CHECK_RESULT", checkPvs);
		
		return "yura/draw/select/jsonResultData";
	}
*/	
	
	
	/**
	 * 3D도면 등록 처리(폴더생성하여 한꺼번에 등록되게 처리)
	 *
	 * @param 
	 * @return
	 * @throws Exception
	 */
	public String upload3DFile(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		
		LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    	
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("verprtoid", (String)commandMap.get("verprtoid"));
		
		return "yura/part/insert/jsonProcessResult";
	}
	
	/**
	 * 도면개정리스트
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/draw/select/selectPvsInfo.do")
	public String selectPvsInfo(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("oid", (String)commandMap.get("oid"));
		map.put("name", (String)commandMap.get("pvsname"));
		
	   	List<Map<String, Object>> resultData = null;
	   	try {
	   		resultData = drawMngService.selectPvsInfo(map);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("도면개정리스트 조회 에러");
		}
	   	
	   	model.addAttribute("JSONDataList", resultData);
	   	return "yura/part/select/jsonResultList";
	}
	

    /**
     * 도면상세 화면
     *
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping("/yura/mod/modInformation.do")
    public String modInformation(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception {
    	return "yura/draw/select/modInformation";
    }

	
	/**
	 * 도면정보 조회
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/draw/select/retrieveDrawInfo.do")
	public String retrieveDrawInfo(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		
		LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		HashMap<String, String> map2 = new HashMap<String, String>();
		map.put("modoid", 	(String)commandMap.get("oid"));
		map2.put("relOid", 	(String)commandMap.get("oid"));
	   	List<Map<String, Object>> drawInfo = null;
	   	List<HashMap<String, String>> prjInfo = null;
	   	try {
	   		drawInfo = drawMngService.retrieveDrawInfo(map);
	   		prjInfo = prjService.selectPrjStageByRelObj(map2);
	   		
	   		//사용이력 등록
	   		/*
			Map<String, Object>vo = new HashMap<String, Object>();
			vo.put("refoid", commandMap.get("oid"));
			vo.put("userid", loginVO.getId());
			vo.put("usetype", "R");
			userMngService.insertUseHistory(vo);
			*/
	   		
	   	} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("도면정보 조회 에러");
		}
	   	
	   	model.addAttribute("drawInfo", drawInfo);
	   	model.addAttribute("prjInfo", prjInfo);
	   	return "yura/draw/select/retrieveDrawInfo";
	}

	/**
	 * 도면개정리스트
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/drawing/select/retrieveDrawVersionList.do")
	public String retrieveDrawVersionList(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("dno", (String)commandMap.get("dno"));
		map.put("targetoid", (String)commandMap.get("targetoid"));
	   	List<Map<String, Object>> result = null;
	   	try {
	   		result = drawMngService.retrieveDrawVersionList(map);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("도면개정리스트 조회 에러");
		}
	   	
	   	model.addAttribute("JSONDataList", result);
	   	return "yura/draw/select/jsonResultList";
	}
	
	/**
	 * 도면리스트
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/drawing/select/retrieveDrawList.do")
	public String retrieveDrawList(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("srchBomPartNum", (String)commandMap.get("srchBomPartNum"));
	   	List<Map<String, Object>> result = null;
	   	try {
	   		result = drawMngService.retrieveDrawList(map);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("도면리스트 조회 에러");
		}
	   	
	   	model.addAttribute("JSONDataList", result);
	   	return "yura/draw/select/jsonResultList";
	}
	
	/**
	 * 도면개정시 Ebom 하위도면 버전 선택 리스트
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/drawing/select/retrieveEbomDrawVerChkList.do")
	public String retrieveEbomDrawVerChkList(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("modoid", (String)commandMap.get("modoid"));
	   	List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
	   	try {
	   		List<Map<String, Object>> ebomList = drawMngService.selectEbomTreeList(map);
	   		if(ebomList != null){
	   			for(int i=0; i < ebomList.size(); i++){
	   				HashMap<String, Object> ebomParamMap = new HashMap<String, Object>();
	   				Map ebomMap = (Map)ebomList.get(i); 
	   				if(ebomMap.get("parentoid") == null || ebomMap.get("parentoid").equals("")) continue;
	   				ebomParamMap.put("dno",(String)ebomMap.get("dno"));
	   				ebomParamMap.put("mversion",(String)ebomMap.get("mversion"));
	   				List<Map<String, Object>> ebomChkList = (List<Map<String, Object>>)drawMngService.retrieveEbomDrawVerChkList(ebomParamMap);
	   				if(ebomChkList != null && ebomChkList.size() > 1){
	   					HashMap<String, Object> resultMap = new HashMap<String, Object>();
	   					String mver = "";
	   					for(int j = 0; j < ebomChkList.size(); j++){
	   						Map ebomChkMap = (Map)ebomChkList.get(j);
	   						if(j == 0){
	   							resultMap.put("prttypename", (String)ebomChkMap.get("prttypename"));
	   							resultMap.put("dno", (String)ebomChkMap.get("dno"));
	   							mver = (String)ebomChkMap.get("modoid")+"|"+(String)ebomChkMap.get("mversion");
	   						}else{
	   							mver = mver+","+(String)ebomChkMap.get("modoid")+"|"+(String)ebomChkMap.get("mversion");
	   						}
	   					}
	   					resultMap.put("mversion",mver);
	   					result.add(resultMap);
	   				}
	   			}
	   		}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("도면개정시 Ebom 하위도면 버전 조회 에러");
		}
	   	model.addAttribute("JSONDataList", result);
	   	return "yura/draw/select/jsonResultList";
	}
	
	/**
	 * 도면과 연결된 부품 사용내역
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/drawing/select/retrieveDrawRelDrawList.do")
	public String retrieveDrawRelDrawList(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("modoid", (String)commandMap.get("modoid"));
	   	List<Map<String, Object>> result = null;
	   	try {
	   		result = drawMngService.retrieveDrawRelDrawList(map);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("도면과 연결된 도면 리스트 조회 에러");
		}
	   	
	   	model.addAttribute("JSONDataList", result);
	   	return "yura/draw/select/jsonResultList";
	}
	
	
	/**
	 * ERP BOM리스트
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/drawing/select/retrieveUnitDrawList.do")
	public String retrieveUnitDrawList(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("verprtoid", (String)commandMap.get("verprtoid"));
		map.put("pno", (String)commandMap.get("pno"));
		
		List<Map<String, Object>> result = null;
	   	try {
	   		result = drawMngService.retrieveUnitDrawList(map);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("ERP BOM리스트 조회 에러");
		}
	   	
		model.addAttribute("JSONDataList", result);
	   	return "yura/draw/select/jsonResultList";
	}
	
	/**
	 * 프로젝트 리스트
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/drawing/select/retrieveRelationModuleInfo.do")
	public String retrieveRelationModuleInfo(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("moid", (String)commandMap.get("moid"));
		map.put("lang", (String)commandMap.get("lang"));
		
		List<Map<String, Object>> result = null;
		try {
			result = drawMngService.retrieveRelationModuleInfo(map);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("프로젝트 리스트 조회 에러");
		}
		
		model.addAttribute("JSONDataList", result);
		return "yura/draw/select/jsonResultList";
	}
	
	/**
	 * 설변정보 리스트
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/drawing/select/retrieveEcPartList.do")
	public String retrieveEcPartList(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("pno", (String)commandMap.get("pno"));
		map.put("pvs", (String)commandMap.get("pvs"));
		map.put("prttype", (String)commandMap.get("prttype"));
		
		List<Map<String, Object>> result = null;
		try {
			result = drawMngService.retrieveEcPartList(map);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("설변정보 조회 에러");
		}
		
		model.addAttribute("JSONDataList", result);
		return "yura/draw/select/jsonResultList";
	}
	
	/**
	 *  3D Sub_Assembly 조회
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/drawing/select/retrieveSubAssemblyInfo.do")
	public String retrieveSubAssemblyInfo(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		HashMap<String, Object> map = new HashMap<String, Object>();
		if(commandMap.get("modoid") != null) map.put("modoid", (String)commandMap.get("modoid"));
		else map.put("modoid", "");
		if(commandMap.get("verprtoid") != null) map.put("verprtoid", (String)commandMap.get("verprtoid"));
		else map.put("verprtoid", "");
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		try {
			result = drawMngService.retrieveSubAssemblyInfo(map);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("파일 조회");
		}
		
		model.addAttribute("JSONDataList", result);
		return "yura/draw/select/jsonResultList";
	}
	
	/**
	 *  파일 이력 조회
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/drawing/select/retrieveSubHistAssemblyInfo.do")
	public String retrieveSubHistAssemblyInfo(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		HashMap<String, Object> map = new HashMap<String, Object>();
		if(commandMap.get("modoid") != null) map.put("modoid", (String)commandMap.get("modoid"));
		else map.put("modoid", "");
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		try {
			result = drawMngService.retrieveSubHistAssemblyInfo(map);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("파일 이력 조회");
		}
		model.addAttribute("JSONDataList", result);
		return "yura/draw/select/jsonResultList";
	}
	
	
	/**
	 *  도면상세조회 - 배포파일수정 클릭
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/draw/update/updateDistAsmFile.do")
	public String updateDistAsmFile(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
		HashMap<String, Object> map = new HashMap<String, Object>();
		List<Map<String, Object>> asmFileList = null;
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		try {
				asmFileList = drawMngService.retrieveAsmFileInfo(commandMap);
				Map<String, Object> tempMap = new HashMap<String, Object>();
				String fileList = (String)commandMap.get("fileList");
				String[] fileData = fileList.split("[,]");
				if(asmFileList!=null && asmFileList.size()>0){
					for(Map<String, Object> fileObj : asmFileList){
						tempMap = new HashMap<String, Object>();
						tempMap.put("modoid", commandMap.get("modoid"));
						tempMap.put("filename", fileObj.get("filename"));
						tempMap.put("distchk", "F");
						/*
						 * 배포도면 체크
						 */
						for (int i = 0; i < fileData.length; i++) {
							if(fileObj.get("filename").equals(fileData[i])){
								tempMap.put("distchk", "T");
								result.add(tempMap);
								break;
							}
						}
						result.add(tempMap);
					}
				}
			drawMngService.updateDistAsmFile(result);
			
			//사용이력 등록
			Map vo = new HashMap();
			vo.put("refoid", commandMap.get("modoid")); //연관oid
			vo.put("userid", loginVO.getId()); //등록자
			vo.put("usetype", "U"); //사용구분 1) C 등록 2) R 조회 3) U 수정 4) D 삭제 5) O 체크아웃 6) I 체크인 7) L 다운로드 중 하나 등록
			vo.put("usedes", "도면상세조회 배포파일수정"); 
			userMngService.insertUseHistory(vo);
			
			model.addAttribute("resultMsg", egovMessageSource.getMessage("success.common.update"));		
			model.addAttribute("result",  "SUCCESS");	

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("배포파일수정 에러");
			model.addAttribute("result",  "FAIL");	
			model.addAttribute("resultMsg",  egovMessageSource.getMessage("fail.common.update"));
		}
		
		return "yura/draw/select/jsonResultData";
	}
	
	
	/**
	 * 체크아웃
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/draw/update/updateCheckOut.do")
	public String updateCheckOut(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> commandMap,
			ModelMap model) throws Exception{
		LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("modoid", (String)commandMap.get("modoid"));
		
		List<Map<String ,Object>> drawInfo = null;
		int checkFlag = 0;
		String source = "";
		String target = "";
		
		try {
			checkFlag = Integer.parseInt(drawMngService.updateCheckOut(request, map)+"");
			
			if(checkFlag>0){
				drawInfo = drawMngService.retrieveDrawInfo(map);
				if(drawInfo != null && drawInfo.size()>0);
					/*
					 * 3D일때 폴더압축 다운로드(CCN00065), 2D중 일부 파일은 폴더에 파일 생성
					 */
					if(drawInfo.get(0).get("subasmcheck").equals("T")){
						
						String filePath = drawInfo.get(0).get("filepath").toString();
						String cmprsTarget = filePath+ ".zip";
						
						source = filePath+ ".zip"; 
						target = filePath+ ".zip"; 
						
						File srcFile = new File(uploadDir + filePath);
						if (srcFile.isDirectory()) {
							File[] fileArr = srcFile.listFiles();
							makeZipFile(fileArr, uploadDirTemp, cmprsTarget);
						}
					}
					
					//사용이력 등록
					Map<String, Object>vo = new HashMap<String, Object>();
				  	
					vo.put("refoid", commandMap.get("modoid"));
					vo.put("userid", loginVO.getId());
					vo.put("usetype", "O");
					vo.put("usedes", "도면상세조회 체크아웃"); //필수값 아니므로, 이력내용 있을시 해당 내용 등록, 단 다운로드, 체크인, 체크아웃시에는 이력 내용에 설명 등록하기
					userMngService.insertUseHistory(vo);
					
					fileDownload(request, target, source, response);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("체크아웃 에러");
		}finally{
			if(drawInfo.get(0).get("subasmcheck").equals("T"))
				EgovFileTool.deleteFile(uploadDirTemp + target);	
		}
		
		return "yura/draw/update/updateCheckOut";
	}
	
	/**
	 * 체크아웃 취소
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/draw/update/updateCheckOutCancel.do")
	public String updateCheckOutCancel(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
		String result = "success";
		String[] modIds = (String[])request.getParameterValues("modIds[]");
		String[] modFileIds = (String[])request.getParameterValues("modFileIds[]");
		try {
			if(modIds != null && modIds.length > 0){
				for(int i = 0; i < modIds.length; i++){
					HashMap<String, Object> paramMap = new HashMap<String, Object>();
					paramMap.put("modoid", modIds[i]);
					paramMap.put("humid", loginVO.getId());
					drawMngService.updateDrawCheckUnlock(paramMap);
				}
			}
			if(modFileIds != null && modFileIds.length > 0){
				for(int j = 0; j < modFileIds.length; j++){
					HashMap<String, Object> paramMap = new HashMap<String, Object>();
					paramMap.put("oid", modFileIds[j]);
					paramMap.put("humid", loginVO.getId());
					drawMngService.updateDrawFileCheckUnlock(paramMap);
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			result = "fail";
			System.out.println("체크아웃 취소 에러");
		}
		
		model.addAttribute("result", result);		
		return "yura/draw/select/jsonResultData";
	}

	/**
	 * 브라우저 구분 얻기.
	 *
	 * @param request
	 * @return
	 */
	private String getBrowser(HttpServletRequest request) {
		String header = request.getHeader("User-Agent");
		if (header.indexOf("MSIE") > -1) {
			return "MSIE";
		} else if (header.indexOf("Trident") > -1) { // IE11 문자열 깨짐 방지
			return "Trident";
		} else if (header.indexOf("Chrome") > -1) {
			return "Chrome";
		} else if (header.indexOf("Opera") > -1) {
			return "Opera";
		}
		return "Firefox";
	}

	
	/**
	 * Disposition 지정하기.
	 *
	 * @param filename
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	private void setDisposition(String filename, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String browser = getBrowser(request);

		String dispositionPrefix = "attachment; filename=";
		String encodedFilename = null;

		if (browser.equals("MSIE")) {
			encodedFilename = URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
		} else if (browser.equals("Trident")) { // IE11 문자열 깨짐 방지
			encodedFilename = URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
		} else if (browser.equals("Firefox")) {
			encodedFilename = "\"" + new String(filename.getBytes("UTF-8"), "8859_1") + "\"";
		} else if (browser.equals("Opera")) {
			encodedFilename = "\"" + new String(filename.getBytes("UTF-8"), "8859_1") + "\"";
		} else if (browser.equals("Chrome")) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < filename.length(); i++) {
				char c = filename.charAt(i);
				if (c > '~') {
					sb.append(URLEncoder.encode("" + c, "UTF-8"));
				} else {
					sb.append(c);
				}
			}
			encodedFilename = sb.toString();
		} else {
			throw new IOException("Not supported browser");
		}

		response.setHeader("Content-Disposition", dispositionPrefix + encodedFilename);

		if ("Opera".equals(browser)) {
			response.setContentType("application/octet-stream;charset=UTF-8");
		}
	}

	
	/**
	 * 체크인
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/draw/update/updateCheckIn.do")
	public String updateCheckIn(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) 
			throws Exception{
		LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
		
		List<Map<String, Object>> drawInfo = null;
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("modoid", (String)commandMap.get("modoid"));
		map.put("filepath",  (String)commandMap.get("filepath"));
		map.put("filename",  "");
		map.put("uploadDir",  uploadDir);

		String filePath = uploadDir + (String)commandMap.get("filepath");
		String filePathBackup = uploadDir + (String)commandMap.get("filepath")+"_backup";//성공적으로 체크인되기전에 백업해놓는 폴더
		String tempFilePath = uploadDirTemp + (String)commandMap.get("filepath")+"_Temp";
		String filename = (String)commandMap.get("orifilename");
		String filesize = (String)commandMap.get("orifilesize");
		String[] arrFilename = filename.split(",");
		String[] arrFilesize = filesize.split(",");
		List<FileVO> fileList = new ArrayList<FileVO>();
		try {
			if (arrFilename.length > 0) {
				//기존에 존재하는 temp폴더가 존재하면 삭제
				EgovFileTool.deleteDirectory(tempFilePath);
				
				if(EgovFileTool.copyDirectory(filePath, filePathBackup)){
					EgovFileTool.deleteDirectory(filePath);
					for(int i=0; i<arrFilename.length;i++){
						FileVO fileVO = new FileVO();
						fileVO.setPhysicalName(arrFilename[i]);
						fileVO.setSize(Long.parseLong(arrFilesize[i]));
						fileVO.setExtName(arrFilename[i].substring(arrFilename[i].lastIndexOf(".")+1, arrFilename[i].length()));
		
						copyFile2(arrFilename[i], arrFilename[i], tempFilePath, filePath);
						fileList.add(fileVO);
					}
					
					int result = drawMngService.updateCheckIn(request, map);
					//asmfile정보를 테이블에 삭제 후 저장
					drawMngService.deleteAsmFileInfo(map);
					drawMngService.uploadAdditionFile3D(fileList, map);
				}else{
					model.addAttribute("resultMsg", egovMessageSource.getMessage("fail.common.insert"));	
				}
			}
			drawInfo = (List<Map<String, Object>>)drawMngService.retrieveDrawInfo(map);
			
			//사용이력 등록
			Map<String, Object>vo = new HashMap<String, Object>();
			vo.put("refoid", commandMap.get("modoid"));
			vo.put("userid", loginVO.getId());
			vo.put("usetype", "I");
			vo.put("usedes", "도면상세조회 체크인");
			userMngService.insertUseHistory(vo);
			
			EgovFileTool.deleteDirectory(filePathBackup);
			
			model.addAttribute("drawInfo", drawInfo);		
			model.addAttribute("resultMsg", egovMessageSource.getMessage("success.common.insert"));		
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("체크인 에러");
			EgovFileTool.deleteDirectory(tempFilePath);
			EgovFileTool.copyDirectory(filePathBackup, filePath);
			model.addAttribute("resultMsg", egovMessageSource.getMessage("fail.common.insert"));		
		}finally{
			EgovFileTool.deleteDirectory(tempFilePath);
		}
		
		model.addAttribute("drawInfo", drawInfo);
		return "yura/draw/update/updateCheckIn";
	}
	
	   
	/**
	 * 도면수정
	 *
	 * @param modoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/draw/update/updateDrawInfo.do")
	public String updateDrawInfo(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("modoid", 	(String)commandMap.get("modoid"));
		map.put("engctgoid", 	(String)commandMap.get("engctgoid"));
		map.put("prttypeoid", 	(String)commandMap.get("prttypeoid"));
		map.put("dno", 	(String)commandMap.get("dno"));
		map.put("dnam", 	(String)commandMap.get("dnam"));
		map.put("mversion",  	(String)commandMap.get("mversion"));
		map.put("modtypeoid",  (String)commandMap.get("modtypeoid"));
		map.put("eono", (String)commandMap.get("eono"));
		map.put("modsizeoid", (String)commandMap.get("modsizeoid"));
		map.put("dscoid", (String)commandMap.get("dscoid"));
		map.put("devstep",  (String)commandMap.get("devstep"));
		String description = URLDecoder.decode((String)commandMap.get("description"), "UTF-8");
		map.put("description", description);
		try {
			drawMngService.updateDrawInfo(request, map);
			model.addAttribute("resultMsg", egovMessageSource.getMessage("success.common.update"));		
			
			//사용이력 등록
			Map<String, Object>vo = new HashMap<String, Object>();
			vo.put("refoid", commandMap.get("modoid"));
			vo.put("userid", loginVO.getId());
			vo.put("usetype", "U");
			userMngService.insertUseHistory(vo);

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("도면 수정 에러");
			model.addAttribute("resultMsg", egovMessageSource.getMessage("fail.common.update"));		
		}
		
		return "yura/draw/update/updateDrawInfo";
	}
	
	/**
	 * 도면삭제
	 *
	 * @param modoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/draw/delete/deleteDrawInfo.do")
	public String deleteDrawInfo(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("modoid", (String)commandMap.get("modoid"));
		
		try {
			String dno = drawMngService.selectModInfo(map);
			int result = drawMngService.deleteDrawInfo(request, map);
			// ebom 의 lastmodoid 갱신 - 원본
			Map<String, Object> updateEbom = new HashMap<String, Object>();
			updateEbom.put("dno", dno);
			String moid = drawMngService.selectMaxModoid(updateEbom);
			updateEbom.put("modoid", moid);			
			if(updateEbom.get("modoid") != null){
				map.put("p_modoid", updateEbom.get("modoid"));
				drawMngService.call_UpdateEbom(map);
			}
			
			//if(result > 0){

				/* 도면상태가 작업중이거나 반송인것만 실제도면정보가 삭제되므로 이력정보를 남김
				if(commandMap.get("staoid").toString().equals("CCN00192") || commandMap.get("staoid").toString().equals("CCN00197")){
					//사용이력 등록
					Map<String, Object>vo = new HashMap<String, Object>();
					vo.put("refoid", commandMap.get("modoid"));
					vo.put("userid", loginVO.getId());
					vo.put("usetype", "D");
					userMngService.insertUseHistory(vo);
				}
				*/
			
				Map<String, Object>vo = new HashMap<String, Object>();
				vo.put("refoid", commandMap.get("modoid"));
				vo.put("userid", loginVO.getId());
				vo.put("usetype", "D");
				userMngService.insertUseHistory(vo);
				
				//결재완룓된 도면중 참조모듈에서 신규등록한 도면
				model.addAttribute("result", "SUCCESSS");		
				model.addAttribute("resultMsg", egovMessageSource.getMessage("success.common.delete"));
			//}else{
			//	model.addAttribute("result", "FAIL");		
			//	model.addAttribute("resultMsg", egovMessageSource.getMessage("fail.common.delete"));
			//}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("도면삭제 에러");
			model.addAttribute("result", "FAIL");		
			model.addAttribute("resultMsg", egovMessageSource.getMessage("fail.common.delete"));		
		}
		
		return "yura/draw/select/jsonResultData";
	}
	
	
	/**
	 * 개정생성
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/draw/insert/insertNewVersion.do")
	public String insertNewVersion(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		HashMap<String, Object> map = new HashMap<String, Object>();
		LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
		map.put("modoid",  (String)commandMap.get("modoid"));
		map.put("mversion",  (String)commandMap.get("mversion"));
		map.put("reghumid", loginVO.getId()); 
		Map<String, Object> result = null;
		try {
			map.put("uploadDir",  uploadDir);
			result = drawMngService.insertNewVersion(map);
			if(result.get("modoid") != null){
				map.put("p_modoid", result.get("modoid"));
				drawMngService.call_UpdateEbom(map);
			}
			model.addAttribute("oid", result.get("modoid"));	
			model.addAttribute("resultMsg", egovMessageSource.getMessage("success.common.insert"));
			
			//사용이력 등록
			Map<String, Object>vo = new HashMap<String, Object>();	
			vo.put("refoid", result.get("modoid"));
			vo.put("userid", loginVO.getId());
			vo.put("usetype", "C");
			vo.put("usedes", "도면상세조회 - 개정생성");
			userMngService.insertUseHistory(vo);
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			System.out.println("개정생성 에러");
			model.addAttribute("resultMsg", egovMessageSource.getMessage("fail.common.insert"));
		}

		return "yura/draw/select/jsonResultData";
	}

	/**
	 * 개정생성시 개정명 Check
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/draw/select/retrieveCheckPvs.do")
	public String retrieveCheckPvs(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("modoid", (String)commandMap.get("modoid"));
		map.put("modtypeoid", (String)commandMap.get("modtypeoid"));
		map.put("pvsname",  (String)commandMap.get("pvsname"));
		map.put("indexno",  (String)commandMap.get("indexno"));
		
		Object checkPvs = null;
		try {
			checkPvs = drawMngService.retrieveCheckDrawPvs(map);
			model.addAttribute("checkvalue", checkPvs);
				 
			if(checkPvs != null && checkPvs.equals("FFF"))
				model.addAttribute("resultMsg", "존재하지 않는 개정입니다.");
			else if(checkPvs != null && checkPvs.equals("isEXIST"))
				model.addAttribute("resultMsg", "해당 개정의 도면이 있습니다.");
			else if(checkPvs != null && checkPvs.equals("ingEXIST"))
				model.addAttribute("resultMsg", "기존에 등록된 해당 도면의 개정중에 작업중이거나 결재진행중인 도면이 있습니다.");
			else
				model.addAttribute("resultMsg", "");
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("개정생성 에러");
		}
		
		return "yura/draw/select/retrieveCheckPvs";
	}
	
	@RequestMapping(value="/draw/select/retrieveMaxDrawCheck.do")
	public String retrieveMaxDrawCheck(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("dno", (String)commandMap.get("dno"));
		map.put("mversion", (String)commandMap.get("mversion"));
		
		Object checkPvs = null;
		try {
			checkPvs = drawMngService.retrieveMaxDrawCheck(map);
			model.addAttribute("checkvalue", checkPvs);
				 
			if(checkPvs != null && checkPvs.equals("FFF"))
				model.addAttribute("resultMsg", "개정생성 할 수 없습니다..");
			else
				model.addAttribute("resultMsg", "");
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("개정생성 에러");
		}
		
		return "yura/draw/select/retrieveCheckPvs";
	}
	
	/**
	 * * 도면 등록 - 1
	 * (도면 등록 모듈에서 도면 등록시 우선 실제 첨부 도면파일을 저장하는 프로세스 진행)
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/drawing/insert/uploadAttachedDrawing.do")
	public String uploadAttachedDrawing(final HttpServletRequest request, ModelMap model
    		,final MultipartHttpServletRequest multiRequest, DrawVO vo) throws Exception  {
		
		try {
			List<FileVO> list = fileUtil.uploadFiles2(request, uploadDir, maxFileSize);
			model.addAttribute("resultMsg", egovMessageSource.getMessage("success.common.delete"));		
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("도면등록 에러");
			model.addAttribute("resultMsg", egovMessageSource.getMessage("fail.common.delete"));		
		}
		
		return "yura/draw/insert/uploadAttachedDrawing";
	}
	
	/**
	 * 도면 상세 정보 모듈에서 파일수정 작업전 임시폴더에 파일업로드
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/drawing/insert/uploadAdditionFile.do")
	public String uploadAdditionFile(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("modoid", (String)commandMap.get("modoid"));
		try{
			SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat ("yyyy", Locale.KOREA);
    		Date cDate = new Date();
    		String currentYear = mSimpleDateFormat.format ( cDate );
    		String realpath = uploadDir+currentYear+File.separator;
			List<FileVO> list  = fileUtil.uploadFiles4(request, realpath, maxFileSize);   
			if(list != null){
				for(FileVO fileVO : list) {
		    		for(int index =0; index<list.size(); index++){
		    			HashMap<String,Object> fileParamMap = new HashMap<String,Object>();
						FileVO vo = list.get(index);  
						String fileOid = drawFileIdgenService.getNextStringId();
						fileParamMap.put("oid", fileOid);
						fileParamMap.put("version", "0");
						fileParamMap.put("modoid", (String)commandMap.get("modoid"));
						fileParamMap.put("humid", loginVO.getId());
						fileParamMap.put("rfilename", vo.getFileName());
						fileParamMap.put("filename", vo.getPhysicalName());
						fileParamMap.put("filesize", vo.getSize());
						fileParamMap.put("ext", vo.getExtName());
						fileParamMap.put("indexno", index+1);
						fileParamMap.put("masterflag", "");
						fileParamMap.put("filepath", currentYear);
					    //도면파일정보 등록
					    drawMngService.insertDrawFileInfo(fileParamMap);
					}	
				}
			}
			model.addAttribute("result", "SUCCESS");		
			model.addAttribute("resultMsg", egovMessageSource.getMessage("success.common.delete"));		
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("파일 업로드 에러");
			model.addAttribute("result", "FAIL");			
			model.addAttribute("resultMsg", egovMessageSource.getMessage("fail.common.delete"));		
		}
		
		return "yura/draw/select/jsonResultData";
	}
	
	/**
	 * 도면 상세 정보 모듈에서 파일추가
	 * (temp폴더 삭제 및 실제폴더에 파일복사 프로세스)
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/draw/insert/addDrawFileInfo.do")
	public String addDrawFileInfo(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("modoid", (String)commandMap.get("modoid"));
		map.put("filepath", (String)commandMap.get("filepath"));
		map.put("filename", "");
		map.put("subasmcheck",  (String)commandMap.get("subasmcheck"));
		map.put("uploadDir",  uploadDir);
		map.put("distdraws",  commandMap.get("distdraws"));
		
		String filePath = uploadDir + (String)commandMap.get("filepath");
		String tempFilePath = uploadDirTemp + (String)commandMap.get("filepath")+"_Temp";
		String filename = (String)commandMap.get("orifilename");
		String filesize = (String)commandMap.get("orifilesize");
		String[] arrFilename = filename.split(",");
		String[] arrFilesize = filesize.split(",");
		List<FileVO> fileList = new ArrayList<FileVO>();
		try{
			if (arrFilename.length > 0) {
				for(int i=0; i<arrFilename.length;i++){
					FileVO fileVO = new FileVO();
					fileVO.setFileName(arrFilename[i]);
					fileVO.setPhysicalName(arrFilename[i]);
					fileVO.setSize(Long.parseLong(arrFilesize[i]));
					fileVO.setExtName(arrFilename[i].substring(arrFilename[i].lastIndexOf(".")+1, arrFilename[i].length()));
					copyFile2(arrFilename[i], arrFilename[i], tempFilePath, filePath);
					fileList.add(fileVO);
				}
				
	
	//			map.put("dbfilename", "");
	//			map.put("rfilename", "");
	//			drawMngService.updateDrawFileInfo(map);
				//asmfile정보를 테이블에 삭제 후 저장
	//			drawMngService.deleteAsmFileInfo(map);
				drawMngService.registerAsmFileInfo(fileList, map);
				
				List<Map<String, Object>> drawInfo = (List<Map<String, Object>>)drawMngService.retrieveDrawInfo(map);
				model.addAttribute("drawInfo", drawInfo);		
				model.addAttribute("resultMsg", egovMessageSource.getMessage("success.common.update"));		
				
				//사용이력 등록
				Map<String, Object>vo = new HashMap<String, Object>();
			  	
				vo.put("refoid", commandMap.get("modoid"));
				vo.put("userid", loginVO.getId());
				vo.put("usetype", "U");
				vo.put("usedes", "도면상세조회 - 파일추가");
				userMngService.insertUseHistory(vo);
				
	//			EgovFileTool.deleteDirectory(filePathBackup);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("파일 수정 에러");
			model.addAttribute("resultMsg", egovMessageSource.getMessage("fail.common.update"));		
		}finally{
			
			if(arrFilename!=null && arrFilename.length>0){
				for(int i=0; i<arrFilename.length;i++){
					EgovFileTool.deleteFile(tempFilePath+arrFilename[i]);
				}
			}
		}
		
		return "yura/draw/select/retrieveDrawInfo";
	}
	
	/**
	 * 도면 상세 정보 모듈에서 파일수정
	 * (temp폴더 삭제 및 실제폴더에 파일복사 프로세스)
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/draw/update/updateDrawFileInfo.do")
	public String uploadDrawFileInfo(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("modoid", (String)commandMap.get("modoid"));
		map.put("filepath", (String)commandMap.get("filepath"));
		map.put("filename", "");
		map.put("subasmcheck",  (String)commandMap.get("subasmcheck"));
		map.put("uploadDir",  uploadDir);
		map.put("distdraws",  commandMap.get("distdraws"));
		
		String filePath = uploadDir + (String)commandMap.get("filepath");
		String filePathBackup = uploadDir + (String)commandMap.get("filepath")+"_backup";//성공적으로 체크인되기전에 백업해놓는 폴더
		String tempFilePath = uploadDirTemp + (String)commandMap.get("filepath")+"_Temp";
		String filename = (String)commandMap.get("orifilename");
		String filesize = (String)commandMap.get("orifilesize");
		String[] arrFilename = filename.split(",");
		String[] arrFilesize = filesize.split(",");
		
		List<FileVO> fileList = new ArrayList<FileVO>();
		try{
			if (arrFilename.length > 0) {
				if(EgovFileTool.copyDirectory(filePath, filePathBackup)){
					EgovFileTool.deleteDirectory(filePath);
					for(int i=0; i<arrFilename.length;i++){
						FileVO fileVO = new FileVO();
						fileVO.setFileName(arrFilename[i]);
						fileVO.setPhysicalName(arrFilename[i]);
						fileVO.setSize(Long.parseLong(arrFilesize[i]));
						fileVO.setExtName(arrFilename[i].substring(arrFilename[i].lastIndexOf(".")+1, arrFilename[i].length()));
		
						copyFile2(arrFilename[i], arrFilename[i], tempFilePath, filePath);
						fileList.add(fileVO);
					}
				}else{
					model.addAttribute("resultMsg", egovMessageSource.getMessage("fail.common.update"));		
				}
			}

			map.put("dbfilename", "");
			map.put("rfilename", "");
			drawMngService.updateDrawFileInfo(map);
			//asmfile정보를 테이블에 삭제 후 저장
			drawMngService.deleteAsmFileInfo(map);
			drawMngService.uploadAdditionFile3D(fileList, map);

			List<Map<String, Object>> drawInfo = (List<Map<String, Object>>)drawMngService.retrieveDrawInfo(map);
			model.addAttribute("drawInfo", drawInfo);		
			model.addAttribute("resultMsg", egovMessageSource.getMessage("success.common.update"));		
			
			//사용이력 등록
			Map<String, Object>vo = new HashMap<String, Object>();
		  	
			vo.put("refoid", commandMap.get("modoid"));
			vo.put("userid", loginVO.getId());
			vo.put("usetype", "U");
			vo.put("usedes", "도면상세조회 - 파일수정");
			userMngService.insertUseHistory(vo);
			
			EgovFileTool.deleteDirectory(filePathBackup);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("파일 수정 에러");
			EgovFileTool.copyDirectory(filePathBackup, filePath);
			model.addAttribute("resultMsg", egovMessageSource.getMessage("fail.common.update"));		
		}finally{
			EgovFileTool.deleteDirectory(tempFilePath);
			EgovFileTool.deleteDirectory(filePathBackup);
		}
		
		return "yura/draw/select/retrieveDrawInfo";
	}
	
	/**
	 * 도면 파일 삭제
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/drawing/delete/deleteDrawFile.do")
	public String deleteDrawFile(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("modoid", (String)commandMap.get("modoid"));
		map.put("oid", (String)commandMap.get("oid"));
		map.put("version", (String)commandMap.get("version"));
		map.put("filename", (String)commandMap.get("filename"));
		map.put("uploadDir",  uploadDir);

		try {  
			drawMngService.deleteDrawFilesInfo(map);
			//파일삭제
			EgovFileTool.deleteFile(uploadDir + commandMap.get("filename"));	

			model.addAttribute("resultMsg", egovMessageSource.getMessage("success.common.delete"));		

			//사용이력 등록
			/*
			Map<String, Object>vo = new HashMap<String, Object>();
		  	
			vo.put("refoid", commandMap.get("modoid"));
			vo.put("userid", loginVO.getId());
			vo.put("usetype", "D");
			vo.put("usedes", "도면상세조회 - 파일삭제");
			userMngService.insertUseHistory(vo);
			*/
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("도면파일 삭제 에러");
			model.addAttribute("resultMsg", egovMessageSource.getMessage("fail.common.delete"));		
		}
		
		return "yura/draw/select/jsonResultData";
	}
	

	/**
	 * 문서 삭제
	 */
	@RequestMapping(value="/drawing/delete/deleteDocInfo.do")
	public String deleteDocInfo(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("verdocoid", (String)commandMap.get("verdocoid"));
		map.put("modoid", (String)commandMap.get("modoid"));
		map.put("moduletype", (String)commandMap.get("moduletype"));
		map.put("ref", (String)commandMap.get("ref"));
		map.put("uploadDir",  uploadDir);
		map.put("serverRoot", request.getSession().getServletContext().getRealPath("/"));
		 
		List<Map<String, Object>> drawInfo = null;
		try {
			int result = drawMngService.deleteDocInfo(map);
			String filePath = request.getSession().getServletContext().getRealPath("/") + uploadDir;
			
			// 문서 첨부 파일 삭제 신규등록(ref="F")된 문서만 파일삭제하도록!
			if ( (result > -1) && "F".equals( commandMap.get("ref") ) )
			{
				List<Map<String, Object>> files = (List<Map<String, Object>>) commandMap.get("files");
				
				for (Map<String, Object> file : files) {
					deleteFile( filePath, (String) file.get("MFILENAME"));
				}		
			}
			
			//사용이력 등록
			Map vo = new HashMap();
			vo.put("refoid", commandMap.get("modoid")); //연관oid
			vo.put("userid", loginVO.getId()); //등록자
			vo.put("usetype", "D"); //사용구분 1) C 등록 2) R 조회 3) U 수정 4) D 삭제 5) O 체크아웃 6) I 체크인 7) L 다운로드 중 하나 등록
			vo.put("usedes", "도면상세정보 - 관련문서 하위기능 문서삭제"); //필수값 아니므로, 이력내용 있을시 해당 내용 등록, 단 다운로드, 체크인, 체크아웃시에는 이력 내용에 설명 등록하기
			userMngService.insertUseHistory(vo);

			model.addAttribute("result", "SUCCESS");		
			model.addAttribute("resultMsg", egovMessageSource.getMessage("success.common.delete"));		
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("문서파일 삭제 에러");
			model.addAttribute("result", "FAIL");		
			model.addAttribute("resultMsg", egovMessageSource.getMessage("fail.common.delete"));		
		}
		
		return "yura/draw/delete/deleteDocInfo";
	}
	
	/**
	 * 문서 선택
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/drawing/insert/insertDrawDocRel.do")
	public String insertDrawDocRel(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
		
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("moduletype", (String)commandMap.get("moduletype"));
		map.put("verdocoid", (String)commandMap.get("verdocoid"));
		map.put("reloid", (String)commandMap.get("reloid"));
		map.put("relmoduleoid", (String)commandMap.get("reloid"));
		
		try {
			int result = drawMngService.insertDrawDocRel(map);
			
			//사용이력 등록
			Map vo = new HashMap();
			vo.put("refoid", commandMap.get("reloid")); //연관oid
			vo.put("userid", loginVO.getId()); //등록자
			vo.put("usetype", "C"); //사용구분 1) C 등록 2) R 조회 3) U 수정 4) D 삭제 5) O 체크아웃 6) I 체크인 7) L 다운로드 중 하나 등록
			vo.put("usedes", "도면상세정보 - 관련문서 하위기능 문서선택"); //필수값 아니므로, 이력내용 있을시 해당 내용 등록, 단 다운로드, 체크인, 체크아웃시에는 이력 내용에 설명 등록하기
			userMngService.insertUseHistory(vo);

			
			model.addAttribute("result", "SUCCESS");	
			if(result==1)
				model.addAttribute("resultMsg", egovMessageSource.getMessage("success.common.insert"));		
			else
				model.addAttribute("resultMsg", egovMessageSource.getMessage("common.isExist.msg"));		
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("문서 선택 에러");
			model.addAttribute("result", "FAIL");
			model.addAttribute("resultMsg", egovMessageSource.getMessage("fail.common.insert"));		
		}
		
		return "yura/draw/insert/insertDrawDocRel";
	}
	
	/**
	 * 문서 첨부 파일 삭제
	 * @param path
	 * @param fileName
	 * @return
	 */
	private boolean deleteFile( String path, String fileName ) {
		File file = new File(path + File.separator + fileName);
		
		if (file.exists())
		{
			return file.delete();
		}
		
		return true;
	}
	
	/**
	 * 마스터파일 체크(subasm file)
	 * @param request
	 * @param commandMap
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/draw/update/updateDrawMasterFile.do")
	private String updateDrawMasterFile(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		HashMap<String, Object> map = new HashMap<String, Object>();
		try {
			if(commandMap.get("filename") != null && !commandMap.get("filename").equals("")
				&& commandMap.get("modoid")!= null && !commandMap.get("modoid").equals("")){
				map.put("filename", commandMap.get("filename"));
				map.put("modoid", commandMap.get("modoid"));
				map.put("oid", commandMap.get("oid"));
				map.put("version", commandMap.get("version"));
				int result = drawMngService.updateDrawMasterFileAllCheckDelete(map);
				result = drawMngService.updateDrawMasterFile(map);
			}
			model.addAttribute("result", "SUCCESS");		
			model.addAttribute("resultMsg", egovMessageSource.getMessage("success.common.insert"));		
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("마스터파일 체크 에러");
			model.addAttribute("result", "FAIL");		
			model.addAttribute("resultMsg", egovMessageSource.getMessage("fail.common.insert"));		
		}
		
		return "yura/draw/select/jsonResultData";
	}
	
	 /**
     * 동일개정정보로 존재하는지 값체크
     **/
	@RequestMapping(value="/draw/select/retrieveCheckExistRev.do")
    public Object retrieveCheckExistRev(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
    	int result = drawMngService.retrieveCheckExistRev(commandMap);
    	if(result > 0)
    		model.addAttribute("result", "Exist");		
    	else
    		model.addAttribute("result", "NotExist");
    	return "yura/draw/select/jsonResultData";
    }
	/***************************************** CAD I/G 서비스 Start ************************************************/
	
	/**
	 * 체크아웃(CAD시스템)
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/cad/draw/selectOpenCAD.do")
	public String selectOpenCAD(HttpServletRequest request, @RequestParam HashMap<String, Object> commandMap, ModelMap model) throws Exception{
		String result = "fail";
		String masterFile = "";
		String filepath = "";
		String checkdate = "";
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		Date today = new Date();
		long checkOutTime = today.getTime();
		HashMap<String, Object> map = new HashMap<String, Object>();
		String modoid = (String)commandMap.get("modoid");
		map.put("modoid", modoid);
		/* *************************************
		 * CAD I/G 체크아웃
		 * *************************************/
		String id = (String)commandMap.get("id");
		String pwd = (String)commandMap.get("pwd");
		map.put("id", (String)commandMap.get("id"));
		map.put("pwd", 	EgovFileScrty.encryptPassword(pwd, id));
		try{
			List<Map<String, Object>> loginCheck = drawMngService.userLoginCheck(map);
			//result = drawMngService.selectCheckOutFlag(commandMap);
			File targetForder = null;
			//if (result != null && result.equals("T") && id != null && pwd != null && loginCheck != null && loginCheck.size() > 0) {
			if (id != null && pwd != null && loginCheck != null && loginCheck.size() > 0) {
				List<Map<String, Object>> ebomList = drawMngService.selectEbomTreeList(map);
				String source = ""; 
				String target = "";
				request.setCharacterEncoding("UTF-8");
				if(ebomList != null){
					File[] fileArr = null;
					List<Map<String, Object>> tempFileList = new ArrayList();
					int fileCnt = 0;
					for(int i = 0; i < ebomList.size(); i++){
						Map ebomMap = (Map)ebomList.get(i);
						String parentoid = (String)ebomMap.get("parentoid");
						String ebomoid = (String)ebomMap.get("modoid");
						String dno = (String)ebomMap.get("dno");
						String mversion = (String)ebomMap.get("mversion");
						HashMap<String, Object> embomParamMap = new HashMap<String, Object>();
						embomParamMap.put("modoid", (String)ebomMap.get("modoid"));
						embomParamMap.put("checkdate", sf.format(checkOutTime));
						if(modoid != null & modoid.equals(ebomoid)){
							filepath = uploadDirCad+dno+"-"+mversion; 
							targetForder = new File(filepath);
							if(!targetForder.exists()){
								targetForder.mkdir();
							}
						}
						List<Map<String, Object>> fileList = drawMngService.retrieveSubAssemblyInfo(embomParamMap);
						if(fileList != null){
							for(int f=0; f < fileList.size(); f++){
								Map fileMap = (Map)fileList.get(f);
								Map tempFileMap = new HashMap();
								String masterflag = (String)fileMap.get("mk");
								if(masterflag == null || masterflag.equals("") || !masterflag.equals("T")) continue;
								String rfilename = (String)fileMap.get("rfilename");
								if((modoid != null & modoid.equals(ebomoid))){
									rfilename =  rfilename;
									masterFile = rfilename;
								}
								String mfilename = (String)fileMap.get("filename");
								tempFileMap.put("rfilename",rfilename);
								tempFileMap.put("filename",mfilename);
								tempFileMap.put("lastmoddate",checkOutTime);
								tempFileList.add(tempFileMap);
								fileCnt ++;
							}
						}
					}
					if(tempFileList != null){
						copyFile3(tempFileList, filepath);
					}
				}
				result = "success";
				//사용이력 등록
				/*
				Map<String, Object>vo = new HashMap<String, Object>(); 	
				vo.put("refoid", commandMap.get("modoid"));
				vo.put("userid", commandMap.get("id"));
				vo.put("usetype", "O");
				vo.put("usedes", "도면상세정보 체크아웃(CAD I/G)"); 
				userMngService.insertUseHistory(vo);
				*/
			}else{
				result = "fail";
			}
			
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println(" 체크아웃(CAD시스템) 에러");
			result = "fail";
		}
		model.addAttribute("result", result);
		model.addAttribute("masterfile", masterFile);
		model.addAttribute("filepath", filepath);
		return "yura/draw/cad/updateCheckOutCAD";
	}
	
	
	/**
	 * 체크아웃(CAD시스템)
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/cad/draw/updateCheckOutCAD.do")
	public String updateCheckOutCAD(HttpServletRequest request, @RequestParam HashMap<String, Object> commandMap, ModelMap model) throws Exception{
		System.out.println("--------------------------check out CAD START-----------------------");
		String link = (String)commandMap.get("link");
		String result = "fail";
		String masterFile = "";
		String filepath = "";
		String checkdate = "";
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		Date today = new Date();
		long checkOutTime = today.getTime();
		HashMap<String, Object> map = new HashMap<String, Object>();
		String modoid = (String)commandMap.get("modoid");
		map.put("modoid", modoid);
		/* *************************************
		 * CAD I/G 체크아웃
		 * *************************************/
		String id = (String)commandMap.get("id");
		String pwd = (String)commandMap.get("pwd");
		map.put("id", (String)commandMap.get("id"));
		map.put("pwd", 	EgovFileScrty.encryptPassword(pwd, id));
		try{
			List<Map<String, Object>> loginCheck = drawMngService.userLoginCheck(map);
			result = drawMngService.selectCheckOutFlag(commandMap);
			File targetForder = null;
			if (result != null && result.equals("T") && id != null && pwd != null && loginCheck != null && loginCheck.size() > 0) {
				List<Map<String, Object>> ebomList = drawMngService.selectEbomTreeList(map);
				if(ebomList != null){
					for(int i = 0; i < ebomList.size(); i++){
						Map ebomMap = (Map)ebomList.get(i);
						String ebomoid = (String)ebomMap.get("modoid");
						String parentoid = (String)ebomMap.get("parentoid");
						String dno = (String)ebomMap.get("dno"); 
						String mversion = (String)ebomMap.get("mversion");
						HashMap<String, Object> embomParamMap = new HashMap<String, Object>();
						embomParamMap.put("modoid", (String)ebomMap.get("modoid"));
						embomParamMap.put("checkdate", sf.format(checkOutTime)); 
						int checkFlag = drawMngService.updateCheckOut(request, embomParamMap);  
					}
				}

				/*
				List<Map<String, Object>> ebomList = drawMngService.selectEbomTreeList(map);
				String source = ""; 
				String target = "";
				request.setCharacterEncoding("UTF-8");
				if(ebomList != null){
					File[] fileArr = null;
					List<Map<String, Object>> tempFileList = new ArrayList();
					int fileCnt = 0;
					for(int i = 0; i < ebomList.size(); i++){
						Map ebomMap = (Map)ebomList.get(i);
						String ebomoid = (String)ebomMap.get("modoid");
						String parentoid = (String)ebomMap.get("parentoid");
						String dno = (String)ebomMap.get("dno");
						String mversion = (String)ebomMap.get("mversion");
						HashMap<String, Object> embomParamMap = new HashMap<String, Object>();
						embomParamMap.put("modoid", (String)ebomMap.get("modoid"));
						embomParamMap.put("checkdate", sf.format(checkOutTime));
						if(modoid != null & modoid.equals(ebomoid)){
							filepath = uploadDirCad+dno+"-"+mversion; 
							targetForder = new File(filepath);
							if(!targetForder.exists()){
								targetForder.mkdir();
							}
						}
						List<Map<String, Object>> fileList = drawMngService.retrieveSubAssemblyInfo(embomParamMap);
						if(fileList != null){
							for(int f=0; f < fileList.size(); f++){
								Map fileMap = (Map)fileList.get(f);
								Map tempFileMap = new HashMap();
								String masterflag = (String)fileMap.get("mk");
								if(masterflag == null || masterflag.equals("") || !masterflag.equals("T")) continue;
								String rfilename = (String)fileMap.get("rfilename");
								if((modoid != null & modoid.equals(ebomoid))){
									rfilename = rfilename;
									masterFile = rfilename;
								}
								String mfilename = (String)fileMap.get("filename");
								tempFileMap.put("rfilename",rfilename);
								tempFileMap.put("filename",mfilename);
								tempFileMap.put("lastmoddate",checkOutTime);
								tempFileList.add(tempFileMap);
								fileCnt ++;
							}
						}
						int checkFlag = drawMngService.updateCheckOut(request, embomParamMap);  
					}
					if(tempFileList != null){
						copyFile3(tempFileList, filepath);
					}
					
				}*/
				result = "success";
				
				//사용이력 등록
				/*
				Map<String, Object>vo = new HashMap<String, Object>(); 	
				vo.put("refoid", commandMap.get("modoid"));
				vo.put("userid", commandMap.get("id"));
				vo.put("usetype", "O");
				vo.put("usedes", "도면상세정보 체크아웃(CAD I/G)"); 
				userMngService.insertUseHistory(vo);
				*/
			}else{
				result = "fail";
			}
			
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println(" 체크아웃(CAD시스템) 에러");
			result = "fail";
		}
		model.addAttribute("result", result);
		model.addAttribute("checkdate", sf.format(checkOutTime));
		model.addAttribute("masterfile", masterFile);
		model.addAttribute("filepath", filepath);
		return "yura/draw/cad/updateCheckOutCAD";
	}
	
	//CAD I/G에서 넘어온 파트  BOM 구성
	@RequestMapping(value="/cad/draw/updateDrawRelPartRevProcess.do")
	public String updateDrawRelPartRevProcess(HttpServletRequest request, @RequestParam HashMap<String, Object> commandMap, ModelMap model) throws Exception{
		String result = "Success";
		try{
			
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("part revision error");
			result = "Fail";
		}
		return result;
	}
	
	@RequestMapping(value="/cad/draw/updateCallBomMain.do")
	public String updateCallBomMain(HttpServletRequest request, @RequestParam HashMap<String, Object> commandMap, ModelMap model) throws Exception{
		String result = "success";
		try{
			drawMngService.updateCallBomMain(commandMap);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("part revision error");
			result = "fail";
		}
		
		model.addAttribute("result", result);
		
		return "yura/draw/cad/updateCheckOutCAD";
	}
	
	/**
	 * MIGRATION PROGRAM
	 *
	 * @param PART,DRAWING,ECO
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/cad/draw/procPdmMig.do")
	public String procPdmMig(HttpServletRequest request, @RequestParam HashMap<String, Object> commandMap, ModelMap model) throws Exception{
		String result = "";
		try{
			result = drawMngService.procPdmMig(commandMap);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("error PDM MIGRATION");
			result = "fail";
		}
		
		model.addAttribute("result", result);
		
		return "yura/draw/cad/updateCheckOutCAD";
	}

	/**
	 * 3D 도면일경우 Dir 명(CAD)
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/cad/draw/getDirName.do")
	public String getDirName(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
	
		HashMap<String, Object> map = new HashMap<String, Object>();
		String id = (String)commandMap.get("id");
		String pwd = (String)commandMap.get("pwd");
		map.put("id", (String)commandMap.get("id"));
		map.put("pwd", 	EgovFileScrty.encryptPassword(pwd, id));
		
		map.put("dno", (String)commandMap.get("dno"));
		map.put("mversion", (String)commandMap.get("mversion"));
		map.put("uploadDir",  uploadDirCad);

		String dirName = "";
		try {
			List<Map<String, Object>> loginCheck = drawMngService.userLoginCheck(map);
			if (loginCheck != null && loginCheck.size()>0) {
				Draw3DFileProcess file = new Draw3DFileProcess(request, uploadDirCad);
				dirName = file.makeDirectory(request, uploadDirCad, map);		
			}
			
			
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("3D 도면일경우 Dir명 조회 에러");
		}
		
		model.addAttribute("filepath", dirName);
		return "yura/draw/cad/getDirNameCAD";
	}
	
	/**
	 * 체크인(CAD)
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/cad/draw/updateCheckInCAD.do")
	public String updateCheckInCAD(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		System.out.println("--------------------------check In CAD START-----------------------");
		String strJson = (String)commandMap.get("list");
		JSONParser parser = new JSONParser();
		JSONObject jsonObj = (JSONObject)parser.parse(strJson);
		JSONArray jsonArr = (JSONArray)jsonObj.get("list");
		HashMap<String, Object> map = new HashMap<String, Object>();
		String modoid = (String)commandMap.get("modoid");
		map.put("modoid", modoid);
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		long changeTime = 0;
		String result = "fail";
		/* *************************************
		 * CAD I/G 체크인
		 * *************************************/
		try {
			List<Map<String, Object>> ebomList = drawMngService.selectEbomTreeList(map);
			System.out.println("ebomList===================>"+ebomList);
			request.setCharacterEncoding("UTF-8");
			String source = ""; 
			String target = "";
			String filepath = "";
			File targetForder = null;
			List<Map<String, Object>> tempFileList = new ArrayList();
			List<Map<String, Object>> embomUpdateInfoList = new ArrayList();
			File[] fileArr = null;
			int fileCnt = 0;
			if(ebomList != null){
				for(int i = 0; i < ebomList.size(); i++){
					Map ebomMap = (Map)ebomList.get(i);
					String ebomoid = (String)ebomMap.get("modoid");
					String dno = (String)ebomMap.get("dno");
					String prttypeoid = (String)ebomMap.get("prttypeoid");
					String moduletype = (String)ebomMap.get("moduletype");
					String modtypeoid = (String)ebomMap.get("modtypeoid");
					String eono = (String)ebomMap.get("eono");
					String mversion = (String)ebomMap.get("mversion");
					String modsizeoid = (String)ebomMap.get("modsizeoid");
					String devstep = (String)ebomMap.get("devstep");
					String staoid = (String)ebomMap.get("staoid");
					String dscoid = (String)ebomMap.get("dscoid");
					String caroid = (String)ebomMap.get("caroid");
					String parentoid = (String)ebomMap.get("parentoid");
					String checkdatestr = (String)ebomMap.get("checkdate");
					if(checkdatestr != null && !checkdatestr.equals("")){
						Date checkdate = sf.parse(checkdatestr);
						changeTime = checkdate.getTime();
					}
					HashMap<String, Object> embomParamMap = new HashMap<String, Object>();
					embomParamMap.put("modoid", ebomoid);
					embomParamMap.put("prttypeoid", prttypeoid);
					embomParamMap.put("dno", dno);
					embomParamMap.put("moduletype", moduletype);
					embomParamMap.put("modtypeoid", modtypeoid);
					embomParamMap.put("eono", eono);
					embomParamMap.put("mversion", mversion);
					embomParamMap.put("modsizeoid", modsizeoid);
					embomParamMap.put("devstep", devstep);
					embomParamMap.put("dscoid", dscoid);
					embomParamMap.put("engctgoid", caroid);
					if(modoid != null && modoid.equals(ebomoid)){
						filepath = uploadDirCad+dno+"-"+mversion; 
						targetForder = new File(filepath);
					}        
					if(targetForder.exists()){   
						List<Map<String, Object>> fileList = drawMngService.retrieveSubAssemblyInfo(embomParamMap);
						System.out.println("fileList===================>"+fileList);
						if(fileList != null){
							for(int f=0; f < fileList.size(); f++){
								Map fileMap = (Map)fileList.get(f);
								Map tempFileMap = new HashMap();
								String masterflag = (String)fileMap.get("mk");
								String rfilename = (String)fileMap.get("rfilename");
								String mfilename = (String)fileMap.get("filename");
								for (int j = 0; j < jsonArr.size(); j++) {          
									Map<String,Object> jsonMap = (Map<String,Object>) jsonArr.get(j);
									String chgMver = (String)jsonMap.get("mversion");
									String chgFileNam = (String)jsonMap.get("filename");
									String id = (String)jsonMap.get("id");
									String pwd = (String)jsonMap.get("pwd");
									embomParamMap.put("humid", id);
									embomParamMap.put("pwd", EgovFileScrty.encryptPassword(pwd, id));
									if(rfilename != null && rfilename.equals(chgFileNam)){
										String fileExt = chgFileNam.substring(chgFileNam.indexOf(".")+1, chgFileNam.length());
										String physicalName = EgovFormBasedFileUtil.getPhysicalFileName()+"."+fileExt;
										tempFileMap.put("rfilename",rfilename);
										tempFileMap.put("filename",physicalName);
										tempFileMap.put("changeTime",changeTime);
										embomParamMap.put("mreversion", chgMver);
										tempFileList.add(tempFileMap);
										fileCnt ++; 
									}
								}    
							}
						}
						String newoid = drawMngService.updateCheckInCAD(embomParamMap, tempFileList);
						if(newoid != null && !newoid.equals("")){
							embomParamMap.put("newoid",newoid);
							embomUpdateInfoList.add(embomParamMap);
						}
					}
				}
				
				//BOM 비교 업데이트 처리
				if(embomUpdateInfoList != null && embomUpdateInfoList.size() > 0){
					for(int e=0; e < embomUpdateInfoList.size(); e++){
						Map ebomUpdateInfoMap = (Map) embomUpdateInfoList.get(e);
						String oid = (String) ebomUpdateInfoMap.get("newoid");
						HashMap<String, Object> ebomUpdateMap = new HashMap<String, Object>();
						ebomUpdateMap.put("modoid",oid);
						List<Map<String, Object>> relBomList = drawMngService.selectRelEbomInfo(ebomUpdateMap);
						if(relBomList != null && relBomList.size() > 0){
							for(int r = 0; r < relBomList.size(); r++){
								Map relBomMap = (Map) relBomList.get(r);
								String sModoid = (String)relBomMap.get("modoid");
								for(int u=e+1; u < embomUpdateInfoList.size(); u++){
									Map compareEbomUpdateInfoMap = (Map) embomUpdateInfoList.get(u);
									String newoid = (String)compareEbomUpdateInfoMap.get("newoid");
									String tModoid = (String)compareEbomUpdateInfoMap.get("modoid");
									if(sModoid != null && tModoid != null && sModoid.equals(tModoid)){
										ebomUpdateMap.put("newmodoid",newoid);
										ebomUpdateMap.put("modoid",sModoid);   
										ebomUpdateMap.put("parentoid",oid);
										drawMngService.updateCheckInCADBomUpdate(ebomUpdateMap);
									}
								}
							}
						}
					}
				}
				
				if(tempFileList != null){
					copyFile4(tempFileList, filepath);
					if(targetForder.exists()){
						File[] destroy = targetForder.listFiles(); 
						for(File des : destroy){
							des.delete(); 
						}	
						targetForder.delete();
					}
				}
				    
				result = "success";
			}
			//사용이력 등록
			/*
			Map<String, Object>vo = new HashMap<String, Object>();	
			vo.put("refoid", commandMap.get("modoid"));
			vo.put("userid", commandMap.get("id"));
			vo.put("usetype", "I");
			vo.put("usedes", "도면상세정보 체크인(CAD I/G)"); 
			userMngService.insertUseHistory(vo);
			*/
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			System.out.println(" 체크인(CAD) 에러");
			result = "fail";
		}
		model.addAttribute("result", result);  
		model.addAttribute("masterfile", "");
		model.addAttribute("filepath", "");
		return "yura/draw/cad/updateCheckInCAD";
	}
	
	/**
	 * 도면리스트 반환(CAD)
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/cad/draw/select/searchDrawListCAD.do")
	public String searchDrawListCAD(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		String id = (String)commandMap.get("id");
		String pwd = (String)commandMap.get("pwd");
		map.put("id", (String)commandMap.get("id"));
		map.put("pwd", 	EgovFileScrty.encryptPassword(pwd, id));
		map.put("engctgoid", (String)commandMap.get("engctgoid"));			//차종정보		
		map.put("prttypeoid", (String)commandMap.get("prttypeoid"));		//제품구분		
		map.put("dno", (String)commandMap.get("dno"));						//도면번호		
		map.put("mversion", (String)commandMap.get("mversion"));			//도면개정		
		map.put("moduletype", (String)commandMap.get("moduletype"));		//도면구분		
		map.put("modtypeoid", (String)commandMap.get("modtypeoid"));		//도면종류		
		map.put("eono", (String)commandMap.get("eono"));					//EONO		
		map.put("modsizeoid", (String)commandMap.get("modsizeoid"));		//도면크기		
		map.put("devstep", (String)commandMap.get("devstep"));				//개발단계		
		map.put("startdate", (String)commandMap.get("startdate"));			//시작일		
		map.put("enddate", (String)commandMap.get("enddate"));				//종료일	
		map.put("username", (String)commandMap.get("username"));			//작성자		
		map.put("staoid", (String)commandMap.get("staoid"));				//상태		
				
		List<Map<String, Object>> drawList = null;
		try {
			List<Map<String, Object>> loginCheck = drawMngService.userLoginCheck(map);
			if (loginCheck != null && loginCheck.size()>0) {
				drawList = drawMngService.retrieveDrawSearchList(map);
				model.addAttribute("JSONDataList", drawList);		
				model.addAttribute("resultMsg", egovMessageSource.getMessage("success.common.delete"));
			}else{
				model.addAttribute("JSONDataList", drawList);		
				model.addAttribute("resultMsg", egovMessageSource.getMessage("fail.common.delete"));		
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("도면리스트 반환(CAD) 에러");
			model.addAttribute("JSONDataList", drawList);		
			model.addAttribute("resultMsg", egovMessageSource.getMessage("fail.common.delete"));		
		}
		
		return "yura/draw/cad/searchDrawListCAD";
	}
	
	/**
	 * 도면리스트 검색조건 반환(CAD)
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/cad/draw/searchDrawCondition.do")
	public String searchDrawCondition(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		
		List<Map<String, Object>> moduletype = null;
		List<Map<String, Object>> engctg = null;
		List<Map<String, Object>> modtypeoid = null;
		List<Map<String, Object>> modsizeoid = null;
		List<Map<String, Object>> devstep = null;
		List<Map<String, Object>> staoid = null;
		List<Map<String, Object>> datecondition = null;
		List<Map<String, Object>> prttypeoid = null;
		HashMap<String, Object> map = new HashMap<String, Object>();
		String id = (String)commandMap.get("id");
		String pwd = (String)commandMap.get("pwd");
		map.put("id", (String)commandMap.get("id"));
		map.put("pwd", 	EgovFileScrty.encryptPassword(pwd, id));
		
		List<Map<String, Object>> loginCheck = drawMngService.userLoginCheck(map);
		if (loginCheck != null && loginCheck.size()>0) {
			prttypeoid	= partMngService.selectCommonCode("CCN11670");	//제품구분
			moduletype	= partMngService.selectCommonCode("CCN00055");	//도면구분
			modtypeoid 	= partMngService.selectCommonCode("CCN00058");	//도면종류
			modsizeoid 	= partMngService.selectCommonCode("CCN00067");	//도면크기
			devstep = partMngService.selectCommonCode("CCN11677");		//개발단계
			engctg	= drawMngService.selectCatEngList(map);//엔진정보
			staoid 	= partMngService.selectCommonCode("CCN00190");//도면상태
			datecondition = partMngService.selectCommonCode("CCN00137");//날짜조건
		}		
		model.addAttribute("prttypeoid", prttypeoid);		
		model.addAttribute("moduletype", moduletype);		
		model.addAttribute("modtypeoid", modtypeoid);		   
		model.addAttribute("modsizeoid", modsizeoid);		
		model.addAttribute("devstep", devstep);
		model.addAttribute("engctg", engctg);	
		model.addAttribute("staoid", staoid);		
		model.addAttribute("datecondition", datecondition);		
		
		return "yura/draw/cad/searchDrawCondition";
	}
	
	/**
	 * 파트 리스트 반환(CAD)
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/cad/draw/searchPartListCAD.do")
	public String searchPartListCAD(HttpServletRequest request, HttpServletResponse response, 
			@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		 
		HashMap<String, Object> map = new HashMap<String, Object>();
		String id = (String)commandMap.get("id");
		String pwd = (String)commandMap.get("pwd");
		map.put("id", (String)commandMap.get("id"));
		map.put("pwd", 	EgovFileScrty.encryptPassword(pwd, id));

		map.put("parttype", 	(String)commandMap.get("parttype"));//품목구분
		map.put("engctg", 		(String)commandMap.get("engctgoid"));	//차종
		map.put("prtctg", 		(String)commandMap.get("itemoid"));	//ITEM
		map.put("site", 		(String)commandMap.get("site"));	//기본공장
		map.put("pno",  		(String)commandMap.get("pno"));		//품번		
		map.put("partname", 	(String)commandMap.get("partname"));//품명
//		map.put("startdate",	(String)commandMap.get("startdate"));//시작일
//		map.put("enddate",  	(String)commandMap.get("enddate"));//종료일
		map.put("username", 	(String)commandMap.get("username"));//작업자

		List<Map<String, Object>> partList = null;
		try {
			List<Map<String, Object>> loginCheck = drawMngService.userLoginCheck(map);
			if (loginCheck != null && loginCheck.size()>0) {
				partList = drawMngService.retrievePartSearchList(map);
				model.addAttribute("JSONDataList", partList);		
				model.addAttribute("resultMsg", egovMessageSource.getMessage("success.common.delete"));		
			}else{
				model.addAttribute("JSONDataList", partList);		
				model.addAttribute("resultMsg", egovMessageSource.getMessage("fail.common.delete"));		
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			model.addAttribute("JSONDataList", partList);		
			model.addAttribute("resultMsg", egovMessageSource.getMessage("fail.common.delete"));		
		}
		
		return "yura/draw/cad/searchPartListCAD";
	}
	
	/**
	 * 파트 리스트 검색조건 반환(CAD)
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/cad/draw/searchPartCondition.do")
	public String searchPartConditionCAD(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		
		List<Map<String, Object>> parttype = null;
		List<Map<String, Object>> prtctg = null;
		List<Map<String, Object>> engctg = null;
		List<Map<String, Object>> devstep = null;
		List<Map<String, Object>> site = null;
		HashMap<String, Object> map = new HashMap<String, Object>();
		String id = (String)commandMap.get("id");
		String pwd = (String)commandMap.get("pwd");
		map.put("id", (String)commandMap.get("id"));
		map.put("pwd", 	EgovFileScrty.encryptPassword(pwd, id));
		/*
		List<Map<String, Object>> loginCheck = drawMngService.userLoginCheck(map);
		if (loginCheck != null && loginCheck.size()>0) {
			parttype	= partMngService.selectCommonCode("CCN00044");//품목구분
			prtctg 		= partMngService.selectCommonCode("CCN03102");//ITEM
			engctg		= drawMngService.selectCatEngList(map);//차종
//			devstep 	= partMngService.selectCommonCode("CCN00047");//개발단계
			site 		= partMngService.selectCommonCode("CCN00036");//기본공장
		}
		model.addAttribute("parttype", parttype);		
		model.addAttribute("prtctg", prtctg);		
		model.addAttribute("engctg", engctg);		
//		model.addAttribute("devstep", devstep);		
		model.addAttribute("site", site);		
		*/
		return "yura/draw/cad/searchPartCondition";
	}
	
	/**
	 * 도면등록을 위한 화면UI용 정보(CAD)
	 *
	 * @param verprtoid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/cad/draw/insertDrawInfoCAD.do")
	public String insertDrawInfoCAD(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		String id = (String)commandMap.get("id");
		String pwd = (String)commandMap.get("pwd");
		map.put("id", (String)commandMap.get("id"));
		map.put("pwd", 	EgovFileScrty.encryptPassword(pwd, id));
		map.put("teamoid", (String)commandMap.get("teamoid"));
		map.put("humoid", (String)commandMap.get("humoid"));
		map.put("etc1", "cad");
		
		List<Map<String, Object>> prttypeoid = null;
		List<Map<String, Object>> moduletype = null;
		List<Map<String, Object>> modtypeoid = null;
		List<Map<String, Object>> modsizeoid = null;
		List<Map<String, Object>> devstep = null;
		List<Map<String, Object>> caroid = null;

		try {
			List<Map<String, Object>> loginCheck = drawMngService.userLoginCheck(map);
			//if (loginCheck != null && loginCheck.size()>0) {
				prttypeoid	= partMngService.selectCommonCode("CCN11670");	//제품구분
				moduletype	= partMngService.selectCommonCode("CCN00055");	//도면구분
				modtypeoid 	= partMngService.selectCommonCode("CCN00058");	//도면종류
				modsizeoid 	= partMngService.selectCommonCode("CCN00067");	//도면크기
				devstep = partMngService.selectCommonCode("CCN11677");		//개발단계
				caroid 	= drawMngService.selectCatEngList(map);				//차종
			//}

			model.addAttribute("prttypeoid", prttypeoid);		
			model.addAttribute("moduletype", moduletype);		
			model.addAttribute("modtypeoid", modtypeoid);		   
			model.addAttribute("modsizeoid", modsizeoid);		
			model.addAttribute("devstep", devstep);
			model.addAttribute("engctgoid", caroid);	
			
			model.addAttribute("resultMsg", egovMessageSource.getMessage("success.common.delete"));		
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			model.addAttribute("resultMsg", egovMessageSource.getMessage("fail.common.delete"));		
		}
		
		return "yura/draw/cad/insertDrawInfoCAD";
	}
	
	/**
	 * 도면정보 등록(CAD I/G용)
	 *
	 * @param 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/cad/draw/registerDrawInfoCAD.do")
	public String registerDrawInfoCAD(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		System.out.println("--------------------------SAVE CAD START-----------------------");
		String strJson = (String)commandMap.get("list");
		JSONParser parser = new JSONParser();
		JSONObject jsonObj = (JSONObject)parser.parse(strJson);
		JSONArray jsonArr = (JSONArray)jsonObj.get("list");
		System.out.println(jsonArr);
		String filename = "";
		String[] filelist = null; 
		String filepath = ""; 
		String moid = "";
		List bomList = new ArrayList();
		/*
		 * 도면기본정보 
		 */
		try{
			if(jsonArr != null && jsonArr.size() > 0){
				for (int j = 0; j < jsonArr.size(); j++) {       
					HashMap<String, Object> map = new HashMap<String, Object>();
					Map<String,Object> jsonMap = (Map<String,Object>) jsonArr.get(j);
					String id = (String)jsonMap.get("id");
					String pwd = (String)jsonMap.get("pwd");
					map.put("id", (String)jsonMap.get("id"));
					map.put("pwd", 	EgovFileScrty.encryptPassword(pwd, id));
					map.put("uid", 	(String)jsonMap.get("uid"));
					map.put("parentuid", 	(String)jsonMap.get("parentuid"));
					map.put("modsizeoid", 	(String)jsonMap.get("modsizeoid"));
					map.put("modtypeoid", 	(String)jsonMap.get("modtypeoid"));
					map.put("moduletype", 	(String)jsonMap.get("moduletype"));
					map.put("eono", 		(String)jsonMap.get("eono"));
					map.put("prttypeoid", 	(String)jsonMap.get("prttypeoid"));
					map.put("engctgoid", 	(String)jsonMap.get("engctgoid"));
					map.put("dno", 			(String)jsonMap.get("dno"));
					map.put("mversion", 	(String)jsonMap.get("mversion"));
					map.put("reloid", 		(String)jsonMap.get("reloid"));
					map.put("devstep", 		(String)jsonMap.get("devstep"));
					map.put("filename", 	(String)jsonMap.get("filename"));
					map.put("dscoid", 		"");
					map.put("disthumid", 	"");
					map.put("humid", 		id);
					filepath = uploadDirCad+(String)jsonMap.get("dno")+"-"+(String)jsonMap.get("mversion")+File.separator;
					filename = (String)jsonMap.get("filename");  					//마스터파일
					filelist = jsonMap.get("filelist").toString().split(",");
					List<FileVO> fileList = new ArrayList<FileVO>();
					if (filelist.length > 0) {
						for(int i=0; i < filelist.length; i++){
							String fileExt = filelist[i].substring(filelist[i].indexOf(".")+1, filelist[i].length());
							String physicalName = EgovFormBasedFileUtil.getPhysicalFileName()+"."+fileExt;
							FileVO fileVO = new FileVO();
							fileVO.setPhysicalName(physicalName);
							fileVO.setFileName(filelist[i]);
							fileVO.setSize(0);
							fileVO.setExtName(fileExt);
							copyFile2( filelist[i], physicalName, filepath, uploadDir);
							fileList.add(fileVO);
							EgovFileTool.deleteFile(filepath + filelist[i]);	
						}
					}
					if(j == 0) {
						moid = drawMngService.registertDrawInfoCAD(fileList, map);
						map.put("modoid", moid);
					}else{
						String oid = drawMngService.registertDrawInfoCAD(fileList, map);
						map.put("modoid", oid);
					}
					bomList.add(j, map);
				}
			}
			//EBOM 등록 처리
			if(bomList != null){
				for(int i = 0; i < bomList.size(); i++){
					HashMap<String, Object> imap = (HashMap)bomList.get(i);
					String uid = (String)imap.get("uid");
					String parentuid = (String)imap.get("parentuid");
					if(uid != null && uid.equals("0")) continue;
					for(int j = 0; j < bomList.size(); j++){
						Map jmap = (Map)bomList.get(j);
						String juid = (String)jmap.get("uid");
						if(juid != null && juid.equals(parentuid)){
							imap.put("parentoid", (String)jmap.get("modoid"));
							imap.put("seq", (j+1));
							drawMngService.registertEbomInfo(imap);
							break;
						}
					}
				} 
			}
			if(moid != null && !moid.equals("")){
				model.addAttribute("oid", moid);		
				model.addAttribute("result",  "SUCCESS");
			}else{
				model.addAttribute("oid",  "");
				model.addAttribute("result",  "FAIL");
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println(e);
			model.addAttribute("oid",  "");
			model.addAttribute("result",  "FAIL");
		}
		
		return "yura/draw/cad/registerDrawInfoCAD";
	}

	/**
	 * 잠금해제 처리(CAD I/G용)
	 *
	 * @param 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/cad/draw/updateCADUnlock.do")
	public String updateCADUnlock(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
		String result = "success";
		String msg = "잠금 해제 처리 되었습니다.";
		try {
			drawMngService.updateDrawCheckUnlock(map);
		} catch (Exception e) {
			e.printStackTrace();
			result = "fail";
			msg = "해제 처리 실패하였습니다.";
		}
		model.addAttribute("result", result);
		return "yura/draw/cad/updateCADUnlock";
	}
	
	/**
	 * 잠금처리(CAD I/G용)
	 *
	 * @param 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/cad/draw/updateCADLock.do")
	public String updateCADLock(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
		String result = "success";
		String msg = "잠금 처리 하였습니다.";
		try {
			drawMngService.updateDrawCheckLock(map);
		} catch (Exception e) {
			e.printStackTrace();
			result = "fail";
			msg = "해제 처리 실패하였습니다.";
		}
		model.addAttribute("result", result);
		return "yura/draw/cad/updateCADLock";
	}

	
	/**
	 * 프로젝트 분류
	 *
	 * @param 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/cad/draw/selectPrjCtgSearchCadOnly.do")
	public String selectPrjCtgSearchCadOnly(HttpServletRequest request, @RequestParam HashMap<String, String> commandMap, ModelMap model) throws Exception {
		List<HashMap<String, String>> result = null;
		HashMap<String, Object> map = new HashMap<String, Object>();
		String id = (String)commandMap.get("id");
		String pwd = (String)commandMap.get("pwd");
		map.put("id", (String)commandMap.get("id"));
		map.put("pwd", 	EgovFileScrty.encryptPassword(pwd, id));
		
		List<Map<String, Object>> loginCheck = drawMngService.userLoginCheck(map);
		if (loginCheck != null && loginCheck.size()>0) {
			result = prjService.selectPrjCtgSearchCadOnly(commandMap);
		}
		model.addAttribute("jsonResult", result);
		
		return "yura/draw/cad/jsonResult";
	}
	
	/**
	 *  프로젝트 스테이지 단계
	 *
	 * @param prjMainOid, loginid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/cad/draw/selectPrjStageByPrjMain.do")
	public String selectPrjStageByPrjMainCAD(HttpServletRequest request, @RequestParam HashMap<String, String> commandMap, ModelMap model) throws Exception{
		HashMap<String, Object> map = new HashMap<String, Object>();
		List<HashMap<String, String>> result = null;
		String id = (String)commandMap.get("id");
		String pwd = (String)commandMap.get("pwd");
		map.put("id", (String)commandMap.get("id"));
		map.put("pwd", 	EgovFileScrty.encryptPassword(pwd, id));
		
		List<Map<String, Object>> loginCheck = drawMngService.userLoginCheck(map);
		if (loginCheck != null && loginCheck.size()>0) {
			commandMap.put("prjMainOid", 	commandMap.get("prjmainoid"));
			commandMap.put("loginId", 	commandMap.get("loginid"));
			result = drawMngService.selectPrjStageByPrjMain(commandMap);
		}
		model.addAttribute("jsonResult", result);
		
		return "yura/draw/cad/dtResult";
	}
	
	/**
	 *  프로젝트 스테이지 단계별 작업항목
	 *
	 * @param prjStageOid, div 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/cad/draw/selectPrjWorkListByPrjStage.do")
	public String selectPrjWorkListByPrjStage(HttpServletRequest request, @RequestParam HashMap<String, String> commandMap, ModelMap model) throws Exception{
		
		System.out.println("prj selectPrjWorkListByPrjStage in!!");
		HashMap<String, Object> map = new HashMap<String, Object>();
		List<HashMap<String, String>> result = null;
		String id = (String)commandMap.get("id");
		String pwd = (String)commandMap.get("pwd");
		map.put("id", (String)commandMap.get("id"));
		map.put("pwd", 	EgovFileScrty.encryptPassword(pwd, id));
		
		List<Map<String, Object>> loginCheck = drawMngService.userLoginCheck(map);
		if (loginCheck != null && loginCheck.size()>0) {
			commandMap.put("prjStageOid", commandMap.get("prjstageoid"));
			commandMap.put("div", "DR");
			result = prjService.selectPrjWorkListByPrjStage(commandMap);
		}
		model.addAttribute("jsonResult", result);
		
		return "yura/draw/cad/dtResult";
	}
	
	/**
	 * CAD I/G 로그인시 사용자 정보 & 권한정보 반환
	 */
	@RequestMapping(value="/cad/userLoginCheck.do")
	public String userLoginCheck(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		
		System.out.println("CharacterEncoding:"+request.getCharacterEncoding());
		System.out.println("ContentType:"+request.getContentType());
		System.out.println("Method:"+request.getMethod());
		System.out.println("QueryString:"+request.getQueryString());
		System.out.println("ServerPort:"+request.getServerPort());
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		String id = (String)commandMap.get("id");
		String pwd = (String)commandMap.get("pwd");
		System.out.println("-----------id: "+id);
		System.out.println("-----------pwd: "+pwd);
		
		map.put("id", id);
		
		// 0. ID 존재여부 체크
		List<Map<String, Object>> idCheck = drawMngService.userLoginCheck(map);
		List<Map<String, Object>> authority = null;
		if(idCheck!=null && idCheck.size()>0)
		{
			// 1. 입력한 비밀번호를 암호화한다.
			String enpassword = EgovFileScrty.encryptPassword(pwd, id);
			map.put("pwd", 	enpassword);
			
			// 2. 일반 로그인 처리
			List<Map<String, Object>> loginCheck = drawMngService.userLoginCheck(map);
			if (loginCheck != null && loginCheck.size()>0) {
			    
				/** CAD 라이센스 체크
			     *  return값 처리 유형
			     *  T : 정상
			     *  E : 기한만료
			     *  C : 동시접속자수 초과 */ 
				LoginVO loginVO = new LoginVO();
				loginVO.setId(id);
				String license = userMngService.cadLicenseCheck(loginVO);
				
				//정상
				if("T".equals(license)){
					map.put("cadpwd", 	"Y");
					drawMngService.updateCADLoginCheck(map);
					model.addAttribute("result", "Exist");
					model.addAttribute("resultMsg", "성공적으로 로그인 되었습니다.");
					model.addAttribute("authority", authority);
				//기한만료
				}else if("E".equals(license)){
					model.addAttribute("result", "Expire");
					model.addAttribute("resultMsg", "라이센스 기간이 만료되었습니다. 관리자에게 문의바랍니다.");
				//동시접속자수 초
				}else if("C".equals(license)){
					model.addAttribute("result", "Exceed");
					model.addAttribute("resultMsg", "동시접속자수가 초과되었습니다. 잠시후에 이용바랍니다.");
				}
			}else{
				model.addAttribute("result", "PWDError");
				model.addAttribute("resultMsg", "비밀번호가 틀렸습니다. 다시한번 확인하세요");
			}
		}else{
			model.addAttribute("result", "NotExist");
			model.addAttribute("resultMsg", "존재하지않는 사용자입니다. 다시한번 확인하세요.");
		}
		
		return "yura/draw/cad/userLoginCheck";
	}

	/**
	 * CAD I/G 로그아웃시 cadpwd변경 초기화
	 */
	@RequestMapping(value="/cad/userLogoutCheck.do")
	public String userLogoutCheck(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		String id = (String)commandMap.get("id");
		String pwd = (String)commandMap.get("pwd");
		
		map.put("id", id);
		String enpassword = EgovFileScrty.encryptPassword(pwd, id);
		map.put("pwd", 	enpassword);
		
		List<Map<String, Object>> loginCheck = drawMngService.userLoginCheck(map);
		if (loginCheck != null && loginCheck.size()>0) {
			//cadpwd 변경
			map.put("cadpwd", 	"N");
			drawMngService.updateCADLoginCheck(map);
			
			//라이센스 체크 해제
			LoginVO loginVO = new LoginVO();
			loginVO.setId(id);
			userMngService.cadReset(loginVO);
			
			model.addAttribute("result", "Success");
		}else{
			model.addAttribute("result", "Fail");
		}
		
		return "yura/draw/cad/userLogoutCheck";
	}
	
	/**
	 * yPLM링크
	 */
	@RequestMapping(value="/cad/yPLMLink.do")
	public String yPLMLink(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		String id = request.getParameter("id");
		request.getSession().setAttribute("linkType", "CAD");
		
		model.addAttribute("id", id);
		model.addAttribute("password", "Y");
		return "yura/draw/cad/yPLMLink";
		
	}
	/***************************************** CAD I/G 서비스 END  ************************************************/
	
	//도면파일 다운로드
	@RequestMapping(value="/draw/fileDownload.do")
	public void drawDownloadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
    	request.setCharacterEncoding("UTF-8");
    	try{
			LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
			HashMap<String, Object> map = new HashMap<String, Object>();
			String modoid = (String)commandMap.get("modoid");
			map.put("modoid", modoid);
			List<Map<String, Object>> ebomList = drawMngService.selectEbomTreeList(map);
			String zipFileName = "";
			String source = ""; 
			String target = "";
			File[] fileArr = null;
			List<Map<String, Object>> tempFileList = new ArrayList();
			int fileCnt = 0;
			if(ebomList != null){
				for(int i = 0; i < ebomList.size(); i++){
					Map ebomMap = (Map)ebomList.get(i);
					HashMap<String, Object> embomParamMap = new HashMap<String, Object>();
					embomParamMap.put("modoid", (String)ebomMap.get("modoid"));
					if(modoid != null && modoid.equals((String)ebomMap.get("modoid"))){
						zipFileName = (String)ebomMap.get("dno")+"-"+(String)ebomMap.get("mversion") + ".zip"; 
						source = (String)ebomMap.get("dno")+"-"+(String)ebomMap.get("mversion") + ".zip"; 
						target = (String)ebomMap.get("dno")+"-"+(String)ebomMap.get("mversion") + ".zip"; 
					}
					List<Map<String, Object>> fileList = drawMngService.retrieveSubAssemblyInfo(embomParamMap);
					if(fileList != null){
						for(int f=0; f < fileList.size(); f++){
							Map fileMap = (Map)fileList.get(f);
							Map tempFileMap = new HashMap();
							String rfilename = (String)fileMap.get("rfilename");
							String mfilename = (String)fileMap.get("filename");
							String filepath = fileMap.get("filepath") != null ? (String)fileMap.get("filepath") : "";
							tempFileMap.put("rfilename",rfilename);
							tempFileMap.put("mfilename",mfilename);
							tempFileMap.put("filepath",filepath);
							tempFileList.add(tempFileMap);
							fileCnt ++;
						}
					}
					
				}
				makeZipFile2(tempFileList, uploadDirTemp, zipFileName);
				fileDownload(request, source, target, response);
			}
			if(tempFileList != null){
				fileArr = new File[fileCnt];
				for(int j = 0; j < fileCnt; j++){
					Map tempFileMap = (Map)tempFileList.get(j);
					String rfilename = (String)tempFileMap.get("rfilename");
					File tempFile = new File(uploadDirTemp + rfilename);
					if(tempFile.isFile()){
						tempFile.delete();
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("파일 다운로드 에러");
		}finally{

		}
		
	}    	
	
    //파일 다운로드
    @RequestMapping(value="/draw/fileDownload2.do")
    public void drawDownloadFile2(ModelMap model, HttpServletRequest request , HttpServletResponse response) throws Exception {
    	LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    	request.setCharacterEncoding("UTF-8");

    	String modoid = (String) request.getParameter("modoid");
		String downFileName = (String) request.getParameter("mfilename");
		String orgFileName = (String) request.getParameter("rfilename");
		String filepath = (String) request.getParameter("filepath");
		orgFileName = java.net.URLDecoder.decode(orgFileName, "UTF-8");
		String stordFilePath = uploadDir+filepath;  
		try{
			File file = new File(EgovWebUtil.filePathBlackList(stordFilePath+"/"+downFileName));
			int fSize = (int) file.length();
			if (fSize > 0) {
				BufferedInputStream in = null;
				try {
					in = new BufferedInputStream(new FileInputStream(file));
					String mimetype = "application/x-msdownload";
					response.setContentType(mimetype);
					String convFileName = URLEncoder.encode(orgFileName, "utf-8");
					convFileName = convFileName.replaceAll("\\+", "%20");
					response.setHeader("Content-Disposition", "attachment; filename=" + convFileName + ";");
					response.setContentLength(fSize);
					FileCopyUtils.copy(in, response.getOutputStream());
				} finally {
					EgovResourceCloseHelper.close(in);
				}
				response.getOutputStream().flush();
				response.getOutputStream().close();
			} else {
				//setContentType을 프로젝트 환경에 맞추어 변경
				response.setContentType("application/x-msdownload");
				response.setCharacterEncoding("UTF-8");
				response.setHeader("Content-Disposition:", "attachment; filename=" + new String(orgFileName.getBytes(), "UTF-8"));
				response.setHeader("Content-Transfer-Encoding", "binary");
				response.setHeader("Pragma", "no-cache");
				response.setHeader("Expires", "0");
				PrintWriter printwriter = response.getWriter();
				printwriter.println("<html>");
				printwriter.println("<br><br><br><h2>Could not get file name:<br>"
						+ orgFileName + "</h2>");
				printwriter
						.println("<br><br><br><center><h3><a href='javascript: history.go(-1)'>Back</a></h3></center>");
				printwriter.println("<br><br><br>&copy; webAccess");
				printwriter.println("</html>");
				printwriter.flush();
				printwriter.close();
			}
		}catch(Exception e){
			System.out.println("파일 다운로드시 에러발생");
		}
    }
    
	/**
	 * 파일 다운
	 * @param orgFileName: 실제파일명
	 * @param downFileName : DB파일명(서버에 저장된 파일명)
	 * @param response
	 */
	public void fileDownload(HttpServletRequest request, String orgFileName, String downFileName, HttpServletResponse response) {
		
		String stordFilePath = uploadDirTemp;
		try{

			File file = new File(EgovWebUtil.filePathBlackList(stordFilePath+downFileName));
			String filePath = stordFilePath+orgFileName;
			File uFile = new File(filePath);
			int fSize = (int) file.length();
			if (fSize > 0) {
				String mimetype = "application/zip";
				setDisposition(downFileName, request, response);

				/*
				 * FileCopyUtils.copy(in, response.getOutputStream());
				 * in.close();
				 * response.getOutputStream().flush();
				 * response.getOutputStream().close();
				 */
				BufferedInputStream in = null;
				BufferedOutputStream out = null;

				try {
					in = new BufferedInputStream(new FileInputStream(uFile));
					out = new BufferedOutputStream(response.getOutputStream());

					FileCopyUtils.copy(in, out);
					out.flush();
				} catch (IOException ex) {
					EgovBasicLogger.ignore("IO Exception", ex);
				} finally {
					EgovResourceCloseHelper.close(in, out);
				}

			} else {
				response.setContentType("application/x-msdownload");

				PrintWriter printwriter = response.getWriter();
				
				printwriter.println("<html>");
				printwriter.println("<br><br><br><h2>Could not get file name:<br>" + downFileName + "</h2>");
				printwriter.println("<br><br><br><center><h3><a href='javascript: history.go(-1)'>Back</a></h3></center>");
				printwriter.println("<br><br><br>&copy; webAccess");
				printwriter.println("</html>");
				
				printwriter.flush();
				printwriter.close();
			}
		}catch(Exception e){
			System.out.println("파일 다운로드시 에러발생");
		}
	}
	
	//도면배포 첨부파일 다운로드
	@RequestMapping(value="/drawDist/fileDownload.do")
	public void drawDistDownloadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> commandMap,
			ModelMap model) throws Exception{

		List<Map<String ,Object>> drawInfo = null;
		
		String distfileoidArry[] = ((String)commandMap.get("distfileoid")).split(";");
		String tmpFolderNM = makeFolderNM();
		try {
			for(int i = 0; i < distfileoidArry.length; i ++) {
				String distfileoid = distfileoidArry[i];
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("distfileoid", distfileoid);
				drawInfo = drawMngService.selectDistFileList(map);
				
				copyFile(drawInfo, tmpFolderNM);
			}
			/*
			 * 폴더압축
			 */
			File srcFile = new File(uploadDirTemp + tmpFolderNM);
			if (srcFile.isDirectory()) {
				File[] fileArr = srcFile.listFiles();
				makeZipFile(fileArr, uploadDirTemp, tmpFolderNM + ".zip");
			}
			
			retfileDownload(tmpFolderNM, tmpFolderNM, response);
			
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			System.out.println("파일 다운로드 에러");
		}finally{
			EgovFileTool.deleteDirectory(uploadDirTemp + tmpFolderNM);
			EgovFileTool.deleteFile(uploadDirTemp + tmpFolderNM + ".zip");
		}
	}    	

	private void copyFile(List<Map<String ,Object>> drawInfo, String tmpFolderNM) throws Exception {
		String stordFilePath = uploadDir;
		String tmpStoredFilePath = uploadDirTemp + tmpFolderNM + "/";
		File tmpFolder = new File(tmpStoredFilePath);
		if(!tmpFolder.exists())
			tmpFolder.mkdir();
		
		for(int index = 0 ; index < drawInfo.size(); index++) {
			Map<String, Object> map = drawInfo.get(index);
			String source = (String) map.get("filename");
			String target = (String) map.get("rfilename");
			File file = new File(EgovWebUtil.filePathBlackList(stordFilePath+source));
			String tmpfilePath = tmpStoredFilePath + target;
			File tmpFile = new File(tmpfilePath);
			int fSize = (int) file.length();
			OutputStream out = new BufferedOutputStream(new FileOutputStream(tmpFile));
			BufferedInputStream in = null;
			if (fSize > 0) {
				in = new BufferedInputStream(new FileInputStream(file));
				
				byte[] buffer = new byte[BUFF_SIZE];
				int read = 0;
				while ((read = in.read(buffer)) != -1) {
					out.write(buffer, 0, read);
				}
				out.flush();
			}
			if(in != null)
				in.close();
			if(out != null)
				out.close();
		}
	}

	private void copyFile2(String sourceFileNm, String targetFileNm, String sourceFilePath, String targetFilePath) throws Exception {
		
		File sourceFolder = new File(sourceFilePath);
		if(!sourceFolder.exists())
			sourceFolder.mkdir();

		File targetFolder = new File(targetFilePath);
		if(!targetFolder.exists())
			targetFolder.mkdir();
	
		File sourceFile = new File(EgovWebUtil.filePathBlackList(sourceFilePath+ FileUploadUtil.SEPERATOR + sourceFileNm));
		File targetFile = new File(EgovWebUtil.filePathBlackList(targetFilePath+ FileUploadUtil.SEPERATOR + targetFileNm));
		
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(sourceFile));
		OutputStream out = new BufferedOutputStream(new FileOutputStream(targetFile));
		FileCopyUtils.copy(in, out);
		
		if(in != null)
			in.close();
		if(out != null)
			out.close();
	}
	
	private void copyFile3(List<Map<String ,Object>> drawInfo, String tmpFolderNM) throws Exception {
		String stordFilePath = uploadDir;
		String tmpStoredFilePath = tmpFolderNM + "/";
		File tmpFolder = new File(tmpStoredFilePath);
		if(!tmpFolder.exists())
			tmpFolder.mkdir();
		
		for(int index = 0 ; index < drawInfo.size(); index++) {
			Map<String, Object> map = drawInfo.get(index);
			String source = (String) map.get("filename");
			String target = (String) map.get("rfilename");
			long lastmoddate = (long) map.get("lastmoddate");
			File file = new File(EgovWebUtil.filePathBlackList(stordFilePath+source));
			String tmpfilePath = tmpStoredFilePath + target;
			File tmpFile = new File(tmpfilePath);
			tmpFile.setLastModified(lastmoddate);
			int fSize = (int) file.length();
			OutputStream out = new BufferedOutputStream(new FileOutputStream(tmpFile));
			BufferedInputStream in = null;
			if (fSize > 0) {
				in = new BufferedInputStream(new FileInputStream(file));
				
				byte[] buffer = new byte[BUFF_SIZE];
				int read = 0;
				while ((read = in.read(buffer)) != -1) {
					out.write(buffer, 0, read);
				}
				out.flush();
			}
			if(in != null)
				in.close();
			if(out != null)
				out.close();
		}
	}
	
	private void copyFile4(List<Map<String ,Object>> drawInfo, String tmpFolderNM) throws Exception {
		String tmpStoredFilePath = tmpFolderNM + "/";
		String stordFilePath = uploadDir;
		File tmpFolder = new File(tmpStoredFilePath);
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		if(tmpFolder.exists()){
			for(int index = 0 ; index < drawInfo.size(); index++) {
				Map<String, Object> map = drawInfo.get(index);
				String source = (String) map.get("filename");
				String target = (String) map.get("rfilename");
				long changeTime  = (long) map.get("changeTime");
				File file = new File(EgovWebUtil.filePathBlackList(tmpStoredFilePath+target));
				String strLastModified = sf.format(file.lastModified());
				Date fileDate = sf.parse(strLastModified);
				long lastmodTime = 	fileDate.getTime();
				String tmpfilePath = stordFilePath + source;
				File tmpFile = new File(tmpfilePath);
				int fSize = (int) file.length();
				int rfSize = (int) tmpfilePath.length();
				//if (fSize > 0 && ((changeTime+1000) < lastmodTime)) {
					OutputStream out = new BufferedOutputStream(new FileOutputStream(tmpFile));
					BufferedInputStream in = null;
					in = new BufferedInputStream(new FileInputStream(file));
					
					byte[] buffer = new byte[BUFF_SIZE];
					int read = 0;
					while ((read = in.read(buffer)) != -1) {
						out.write(buffer, 0, read);
					}
					out.flush();
					if(in != null)
						in.close();
					if(out != null)
						out.close();
				//}
			}
		}
	}
	
	/**
	 * 파일 다운
	 * @param orgFileName: 실제파일명
	 * @param downFileName : DB파일명(서버에 저장된 파일명)
	 * @param response
	 */
	public String retfileDownload(String orgFileName, String downFileName, HttpServletResponse response) {
		String isOk = "";
		String stordFilePath = uploadDirTemp;
		
		try{
			File file = new File(EgovWebUtil.filePathBlackList(stordFilePath+downFileName + ".zip"));
			String filePath = stordFilePath+orgFileName;
			
			int fSize = (int) file.length();
			
			if (fSize > 0) {
				BufferedInputStream in = new BufferedInputStream(
						new FileInputStream(file));
				String mimetype = "application/zip"; //application/unknown
//				String mimetype = "text/html";
				response.setContentType("application/octet-stream; charset=utf-8");
				response.setHeader("Content-Disposition", "attachment; filename=\""
						+ orgFileName+".zip" + "\"");
				response.setHeader("Content-Transfer-Encoding","binary");
//				response.setContentLength(fSize);

				byte[] buffer = new byte[BUFF_SIZE]; //buffer size 2K.
				BufferedOutputStream outs = null;
			
				outs = new BufferedOutputStream(response.getOutputStream());
				int read = 0;
		
				while ((read = in.read(buffer)) != -1) {
					outs.write(buffer, 0, read);
				}
				outs.flush();
				if(in != null)
					in.close();
			} else {
				//setContentType을 프로젝트 환경에 맞추어 변경
				response.setContentType("application/x-msdownload");
				PrintWriter printwriter = response.getWriter();
				printwriter.println("<html>");
				printwriter.println("<br><br><br><h2>Could not get file name:<br>"
						+ filePath + "</h2>");
				printwriter
						.println("<br><br><br><center><h3><a href='javascript: history.go(-1)'>Back</a></h3></center>");
				printwriter.println("<br><br><br>&copy; webAccess");
				printwriter.println("</html>");
				printwriter.flush();
				printwriter.close();
			}
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("파일 다운로드시 에러발생");
		}
		return isOk;
	}

	 /***************************************** 도면배포 서비스 Start ************************************************/
		@RequestMapping(value="/yura/draw/select/drawDistribute.do")
		public String drawDistritute(HttpServletRequest request, ModelMap model) {
			return "yura/draw/select/drawDistritute";
		}
		
		@RequestMapping(value="/draw/select/selectDrawDistTeam.do")
		public String selectDrawDistTeam(HttpServletRequest request, ModelMap model) {
			List<Map<String, Object>> result = null;
			try {
				result = drawMngService.selectDrawDistTeam();
			} catch (Exception e) {
				//e.printStackTrace();
			}
			model.addAttribute("JSONDataList", result);
			return "/yura/com/select/selectMenuListSearching";
		}
		
		@RequestMapping(value="/draw/select/selectDistCooperTbl.do")
		public String selectDistCooperTbl(HttpServletRequest request, ModelMap model) {
			List<Map<String, Object>> result = null;
			try {
				result = drawMngService.selectDistCooperTbl();
			} catch (Exception e) {
				//e.printStackTrace();
			}
			model.addAttribute("JSONDataList", result);		
			return "/yura/com/select/selectMenuListSearching";
		}
		
		@RequestMapping(value="/draw/insert/insertDrawDist.do")
		public String insertDrawDist(HttpServletRequest request, ModelMap model) {
			return "yura/draw/insert/insertDrawDist";
		}
		
		@RequestMapping(value="/draw/insert/insertCooperDrawDist.do")
		public String insertCooperDrawDist(HttpServletRequest request, ModelMap model) {
			return "yura/draw/insert/insertCooperDrawDist";
		}
		
		@RequestMapping(value="/draw/insert/insertDistTeamManage.do")
		public String insertDistTeamManage(HttpServletRequest request, ModelMap model) {
			return "yura/draw/insert/insertDistTeamManage";
		}
		
		@RequestMapping(value="/draw/insert/insertCooperManage.do")
		public String insertCooperManage(HttpServletRequest request, ModelMap model) {
			return "yura/draw/insert/insertCooperManage";
		}
		
		@RequestMapping(value="/draw/select/selectCooperManageList.do")
		public String selectCooperManage(HttpServletRequest request, @RequestParam Map<String, Object> map, ModelMap model) {
			List<Map<String, Object>> list = null;
			List<Map<String, Object>> result = null;
			int resultCnt = 0;
			try {
				result = drawMngService.selectCooperManageList(map);
				resultCnt = drawMngService.selectCooperManageListCnt();
			} catch (Exception e) {
				e.printStackTrace();
			}
			model.addAttribute("JSONDataList", 	result);
			model.addAttribute("resultCnt", 	resultCnt);
			return "/yura/ccn/select/selectCommonCodeList";
		}
		
		@RequestMapping(value="/draw/select/selectDrawDistTeamList.do")
		public String selectDrawDistTeamList(HttpServletRequest request, @RequestParam Map<String, Object> map, ModelMap model) {
			List<Map<String, Object>> result = null;
			int resultCnt = 0;
			try {
				result = drawMngService.selectDrawDistTeamList(map);
				resultCnt = drawMngService.selectDrawDistTeamListCnt();
			} catch (Exception e) {
				e.printStackTrace();
			}
			model.addAttribute("JSONDataList", 	result);
			model.addAttribute("resultCnt", 	resultCnt);
			return "/yura/ccn/select/selectCommonCodeList";
		}

		@RequestMapping(value = "/draw/select/selectEmptyJson.do")
		public String selectEmptyJson(HttpServletRequest request, ModelMap model) {
			List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
			model.addAttribute("JSONDataList", result);
			return "/yura/com/select/selectMenuListSearching";
		}

		/* 배포팀 담당자 추가 */
		@RequestMapping(value = "/draw/insert/insertDistTeamHum.do")
		public String insertDistTeamHum(HttpServletRequest request,
				@RequestParam String[] humid, String[] teamoid, ModelMap model) {
			String msg = "등록되었습니다.";
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			for (int i = 0; i < humid.length; i++) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("humid", humid[i]);
				map.put("teamoid", teamoid[i]);
				list.add(map);
			}
			try {
				drawMngService.insertDistTeamHum(list);
			} catch (Exception e) {
				e.printStackTrace();
				msg = "등록에 실패하였습니다.";
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}

		/* 배포팀 담당자 삭제 */
		@RequestMapping(value = "/draw/delete/deleteDistTeamHum.do")
		public String deleteDistTeamHum(HttpServletRequest request,
				@RequestParam String oid, ModelMap model) {
			String msg = "삭제되었습니다.";
			try {
				drawMngService.deleteDistTeamHum(oid);
			} catch (Exception e) {
				e.printStackTrace();
				msg = "삭제 실패하였습니다.";
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}

		@RequestMapping(value = "/draw/delete/deleteDrawDistCom.do")
		public String deleteDrawDistCom(HttpServletRequest request,
				@RequestParam String[] oid, ModelMap model) {
			String msg = "삭제되었습니다.";
			try {
				drawMngService.deleteDrawDistCom(oid);
			} catch (Exception e) {
				e.printStackTrace();
				msg = "삭제 실패하였습니다.";
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}

		/* 
		 * 도면배포 등록
		 * teamcomoid. modtypeoid, dszoid, caroid, pctoid
		 */
		@RequestMapping(value = "/draw/insert/insertDrawingDistMain.do")
		public String insertDrawingRegistrationMain(
				final HttpServletRequest request,
				final MultipartHttpServletRequest multiRequest,
				@RequestParam Map<String, Object> map, 
														 ModelMap model) {
			String msg = "등록되었습니다.";
			LoginVO loginVO = (LoginVO) EgovUserDetailsHelper
					.getAuthenticatedUser();
			try {
				String distoid = drawMngService.insertDrawingRegistrationMain(map);
				String storePathString = EgovProperties.getProperty("Globals.fileStorePath.draw");
				String[] distmodsArry = ((String)map.get("distmods")).split(";");
				for(String distmods : distmodsArry) {
					Map<String, Object> distModMap = new HashMap<String, Object>();
					distModMap.put("oid", distoid);
					distModMap.put("modoid", distmods);
					drawMngService.registertDistModHistoryInfo(distModMap);
				}
				
				File saveFolder = new File(EgovWebUtil.filePathBlackList(storePathString));
				if (!saveFolder.exists() || saveFolder.isFile()) {
					saveFolder.mkdirs();
				}
				String filePath = "";
				String newName = "";
				String seq = "";
				final Map<String, MultipartFile> files = multiRequest.getFileMap();
				List<FileVO> listFile = FileUploadUtil.uploadFiles2(request, storePathString, 1000000);
				for(FileVO fileVO : listFile) {
					Map<String, Object> fileMap = new HashMap<String, Object>();
					fileMap.put("distoid", distoid);
					fileMap.put("humid", loginVO.getId());
					fileMap.put("filename", fileVO.getPhysicalName());
					fileMap.put("rfilename", fileVO.getFileName());
					drawMngService.insertDistAttachFile(fileMap);
				}
				
				String[] dtrTemsArry = ((String)map.get("dtrteams")).split(";");
				for(String dtrteams : dtrTemsArry) {
					Map<String, Object> distModMap = new HashMap<String, Object>();
					distModMap.put("oid", distoid);
					distModMap.put("distteamoid", dtrteams);
					drawMngService.registertDistTeamHistoryInfo(distModMap);
					
	    			/*
	    			 * 배포팀 메일발송(네오텍)
	    			 */
	    			List<Map<String, Object>> distTeamInfo = (List<Map<String, Object>>)drawMngService.selectDrawDistTeamList(distModMap);
	    			if(distTeamInfo != null && distTeamInfo.size()>0){
	    				drawMngService.sendMailDistTeam(distTeamInfo.get(0));
	    			}
				}
				
				//사용이력 등록
				Map<String, Object>vo = new HashMap<String, Object>();
			  	
				vo.put("refoid", distoid);
				vo.put("userid", loginVO.getId());
				vo.put("usetype", "C");
				userMngService.insertUseHistory(vo);
			} catch (Exception e) {
				msg = "등록에 실패하였습니다.";
				e.printStackTrace();
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}

		@RequestMapping(value = "/draw/insert/insertDrawDistCom.do")
		public String insertDrawDistCom(HttpServletRequest request,
				@RequestParam Map<String, Object> map, ModelMap model) {
			String msg = "등록되었습니다.";
			LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
			try {
				String oid = distcomOidGnrService.getNextStringId();
				map.put("oid", oid);
				drawMngService.insertDrawDistCom(map);
				
				//사용이력 등록
				Map<String, Object>vo = new HashMap<String, Object>();
			  	
				vo.put("refoid", oid);
				vo.put("userid", loginVO.getId());
				vo.put("usetype", "C");
				userMngService.insertUseHistory(vo);
			} catch (Exception e) {
				msg = "등록에 실패하였습니다.";
				e.printStackTrace();
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}

		/*
		 * 도면배포 리스트(사내용)
		 */
		@RequestMapping(value = "/draw/select/selectDistSearching.do")
		public String selectDistSearching(HttpServletRequest request,
				@RequestParam Map<String, Object> map, ModelMap model) {
			List<Map<String, Object>> result = null;
			int resultCnt = 0;
			try {
				result = drawMngService.selectDistSearching(map);
				resultCnt = result == null ? 0 : result.size();
			} catch (Exception e) {
				e.printStackTrace();
			}
			model.addAttribute("JSONDataList", result);
			model.addAttribute("resultCnt", resultCnt);
			return "/yura/ccn/select/selectCommonCodeList";
		}

		@RequestMapping(value = "/draw/delete/deleteCooperManage.do")
		public String deleteCooperManage(HttpServletRequest request,
				@RequestParam String oidArry, ModelMap model) {
			String msg = "삭제되었습니다.";
			LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
			try {
				drawMngService.deleteCooperManage(oidArry, loginVO.getId());
			} catch (Exception e) {
				e.printStackTrace();
				msg = "삭제 실패하였습니다.";
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}

		@RequestMapping(value = "/draw/update/updateDrawDistCom.do")
		public String updateDrawDistCom(HttpServletRequest request,
				@RequestParam Map<String, Object> map, ModelMap model) {
			String msg = "수정되었습니다.";
			LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
			try {
				drawMngService.updateDrawDistCom(map);
				//사용이력 등록
				Map<String, Object>vo = new HashMap<String, Object>();
			  	
				vo.put("refoid", map.get("oid"));
				vo.put("userid", loginVO.getId());
				vo.put("usetype", "U");
				userMngService.insertUseHistory(vo);
			} catch (Exception e) {
				e.printStackTrace();
				msg = "수정에 실패하였습니다.";
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
		
		@RequestMapping(value="/draw/select/selectDistInsideList.do")
		public String selectDistInsideList(HttpServletRequest request, @RequestParam String distoid, ModelMap model) {
			List<Map<String, Object>> result = null;
			int resultCnt = 0;
			try {
				result = drawMngService.selectDistInsideList(distoid);
				resultCnt = result == null ? 0 : result.size();
			} catch (Exception e) {
				e.printStackTrace();
			}
			model.addAttribute("JSONDataList", result);
			model.addAttribute("resultCnt", resultCnt);		
			return "/yura/ccn/select/selectCommonCodeList";
		}
		
		@RequestMapping(value="/draw/select/selectDistDrawFileList.do")
		public String selectDistDrawFileList(HttpServletRequest request, @RequestParam String distoid, ModelMap model) {
			List<Map<String, Object>> result = null;
			int resultCnt = 0;
			try {
				result = drawMngService.selectDistDrawFileList(distoid);
				resultCnt = result == null ? 0 : result.size();
			} catch (Exception e) {
				e.printStackTrace();
			}
			model.addAttribute("JSONDataList", result);
			model.addAttribute("resultCnt", resultCnt);		
			return "/yura/ccn/select/selectCommonCodeList";
		}
		
		@RequestMapping(value="/draw/select/selectDistDrawList.do")
		public String selectDistDrawList(HttpServletRequest request, @RequestParam String distoid, ModelMap model) {
			List<Map<String, Object>> result = null;
			int resultCnt = 0;
			try {
				result = drawMngService.selectDistDrawList(distoid);
				resultCnt = result == null ? 0 : result.size();
			} catch (Exception e) {
				e.printStackTrace();
			}
			model.addAttribute("JSONDataList", result);
			model.addAttribute("resultCnt", resultCnt);		
			return "/yura/ccn/select/selectCommonCodeList";
		}
		
		@RequestMapping(value="/draw/select/selectSearchTeamList.do")
		public String selectSearchTeamList(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			List<Map<String, Object>> result = null;
			int resultCnt = 0;
			try {
				result = drawMngService.selectSearchTeamList(map); 
				resultCnt = result == null ? 0 : result.size();
			} catch (Exception e) {
				e.printStackTrace();
			}
			model.addAttribute("JSONDataList", result);
			model.addAttribute("resultCnt", resultCnt);			
			return "/yura/ccn/select/selectCommonCodeList";
		}
		
		@RequestMapping(value="/draw/select/selectDistcomhisryList.do")
		public String selectDistcomhisryList(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			List<Map<String, Object>> result = null;
			try {
				if(map.get("distflag") != null && map.get("distflag").equals("T"))
					result = drawMngService.selectComDistComp(map);
				else
					result = drawMngService.selectDistcomhisryList(map);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			model.addAttribute("JSONDataList", result);
			model.addAttribute("resultCnt", result.size());		
			return "/yura/ccn/select/selectCommonCodeList";
		}
		
		/*
		 * 협력업체 도면 배포
		 */
		@RequestMapping(value="/draw/insert/insertDrawDistComHistory.do")
		public String insertDrawDistComHistory(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			String msg = "배포되었습니다.";
			try {
				drawMngService.insertDrawDistComHistory(map);
				List<Map<String, Object>> list = drawMngService.selectDistTeamEmail(map);
				
				for(Map<String, Object> resultMap : list) {
					String email = (String) resultMap.get("email");
					String name = (String) resultMap.get("name");
					
					String content = "<br> yPLM 시스템에서 도면이 배포되었습니다.";
					content += "<br><br> 사용자 성명:"+ name;
					content += "<br><br> 사용자 이메일:"+ email;
					content += "<br><br><br> ※ 본 메일은 수신 전용입니다.";
//					content += "<br><br><a href='"+systemURL+"' target='_blank'>→ yPLM시스템 바로가기</a>";

					//메일 발송
					SndngMailVO sndngMailVO = new SndngMailVO();
			    	sndngMailVO.setDsptchPerson("yPlmMaster");
			    	sndngMailVO.setRecptnPerson(email);
			    	sndngMailVO.setSj("[yPLM] 도면배포 알림");
			    	sndngMailVO.setEmailCn(content);
			    	sndngMailVO.setAtchFileId(""); 
			    	/*
			    	sndngMailVO.setAtchFileId("AF"); //첨부파일 발송을 위해 값을 AF라 설정함.
			    	String filepath = EgovProperties.getProperty("Globals.fileStorePath.ec")+"ecr_sample.xlsx";
			    	sndngMailVO.setFileStreCours(filepath);
			    	sndngMailVO.setOrignlFileNm("ecTestFile.xlsx");
			    	*/
			    	sndngMailRegistService.insertSndngMailH(request, sndngMailVO);				
				}
				
				String emailArry[] = ((String)map.get("distcomemail")).split(";");
				String nameArry[] = ((String)map.get("distcomname")).split(";");
				for(int i = 0; i < emailArry.length; i ++) {
					String email = emailArry[i];
					String name = nameArry[i];
					String content = "<br> yPLM 시스템에서 도면 배포 요청이 접수되었습니다.";
					content += "<br><br> 사용자 성명:"+ name;
					content += "<br><br> 사용자 이메일:"+ email;
					content += "<br><br><br> ※ 본 메일은 수신 전용입니다.";
//					content += "<br><br><a href='"+systemURL+"' target='_blank'>→ yPLM시스템 바로가기</a>";
					
					//메일 발송
					SndngMailVO sndngMailVO = new SndngMailVO();
			    	sndngMailVO.setDsptchPerson("yPlmMaster");
			    	sndngMailVO.setRecptnPerson(email);
			    	sndngMailVO.setSj("[yPLM] 도면배포 알림");
			    	sndngMailVO.setEmailCn(content);
			    	sndngMailVO.setAtchFileId(""); 
			    	/*
			    	sndngMailVO.setAtchFileId("AF"); //첨부파일 발송을 위해 값을 AF라 설정함.
			    	String filepath = EgovProperties.getProperty("Globals.fileStorePath.ec")+"ecr_sample.xlsx";
			    	sndngMailVO.setFileStreCours(filepath);
			    	sndngMailVO.setOrignlFileNm("ecTestFile.xlsx");
			    	*/
			    	sndngMailRegistService.insertSndngMailH(request, sndngMailVO);				
				}
			} catch(YuraException e) { 
				msg = "배포 권한이 없습니다.";
				e.printStackTrace();
			} catch (Exception e) {
				msg = "배포에 실패하였습니다.";
				e.printStackTrace();
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
		
		/*
		 * 도면조회시 도면조회이력 등록(도면배포접수확인용 이력)
		 */
		@RequestMapping(value="/draw/insert/insertModhistory.do")
		public String insertModhistory(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			String msg = "등록되었습니다.";
			try {
				drawMngService.insertModhistory(map);
			} catch (Exception e) {
				e.printStackTrace();
				msg = "등록에 실패하였습니다.";
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
		
		@RequestMapping(value="/draw/insert/insertDrawingFile.do")
		public String insertDrawFile(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			String msg = "등록되었습니다.";
			String storePathString = EgovProperties.getProperty("Globals.fileStorePath.draw");
			try {
//				drawMngService.insertDrawFile(map);
				List<FileVO> listFile = FileUploadUtil.uploadFilesEtc(request, storePathString, 1000000);
				drawMngService.insertDrawFileList(listFile, map);
			} catch (Exception e) {
				e.printStackTrace();
				msg = "등록에 실패하였습니다.";
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";		
		}
		
		@RequestMapping(value="/draw/delete/deleteDistFile.do")
		public String deleteDistFile(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			String msg = "삭제되었습니다.";
			try {
				drawMngService.deleteDistFile(map);
			} catch (Exception e) {
				e.printStackTrace();
				msg = "삭제 시 에러가 발생하였습니다.";
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
		
		@RequestMapping(value="/draw/select/selectCcnUnitList.do")
		public String selectCcnUnitList(HttpServletRequest request, ModelMap model, @RequestParam(value="parentoid") String parentoid) {
			List<Map<String, Object>> list = null;
			try {
				list = drawMngService.selectCcnUnitList(parentoid);
			} catch (Exception e) {
				e.printStackTrace();
			}
			model.addAttribute("JSONDataList", list);
	    	return "yura/team/select/jsonResultList";
		}
		
		//DrawMngDAO.selectTeamcomList
		@RequestMapping(value="/draw/select/selectTeamcomList.do")
		public String selectTeamcomList(HttpServletRequest request, ModelMap model) {
			List<Map<String, Object>> list = null;
			try {
				list = drawMngService.selectTeamcomList();
			} catch (Exception e) {
				e.printStackTrace();
			}
			model.addAttribute("JSONDataList", list);
			return "yura/team/select/jsonResultList";
		}
		
		/*
		 * 배포도면 정보 수정
		 */
		@RequestMapping(value="/draw/update/updateDistHistory.do")
		public String updateDistHistory(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			String msg = "수정되었습니다.";
			LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
			try {
				drawMngService.updateDistHistory(map);
				//사용이력 등록
				Map<String, Object>vo = new HashMap<String, Object>();
			  	
				vo.put("refoid", map.get("distoid"));
				vo.put("userid", loginVO.getId());
				vo.put("usetype", "U");
				vo.put("usedes", "도면배포 배포정보 수정"); 
				userMngService.insertUseHistory(vo);
			} catch (Exception e) {
				e.printStackTrace();
				msg = "수정 시 에러가 발생하였습니다.";
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
		
		
		/*
		 * 배포도면 정보 삭제
		 */
		@RequestMapping(value="/draw/delete/deleteDistHistory.do")
		public String deleteDistHistory(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			String msg = "삭제되었습니다.";
			LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
			try {
				drawMngService.deleteDistHistory(map);
				
				//사용이력 등록
				Map<String, Object>vo = new HashMap<String, Object>();
			  	
				vo.put("refoid", map.get("distoid"));
				vo.put("userid", loginVO.getId());
				vo.put("usetype", "D");
				vo.put("usedes", "도면배포 배포도면 삭제"); 
				userMngService.insertUseHistory(vo);
			} catch (Exception e) {
				e.printStackTrace();
				msg = "삭제 시 에러가 발생하였습니다.";
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
		
		/*
		 * 도면배포 배포도면 다운로드
		 */
		@RequestMapping(value="/draw/selectDistFileDownload.do")
		public void selectDistFileDownload(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> commandMap) {
			LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
			String modoidArry[] = ((String)commandMap.get("modoid")).split(";");
			String tmpFolderNM = makeFolderNM();
			String cmprsPath = uploadDirTemp + tmpFolderNM;
			File srcFile;
			try {
				for(int i = 0; i < modoidArry.length; i++) {
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("modoid", modoidArry[i]);
					
					List<Map<String ,Object>> drawInfo = drawInfo = drawMngService.retrieveDrawInfo(map);
					if(drawInfo != null && drawInfo.size()>0){
						/*
						 * 3D일때 폴더압축 다운로드(CCN00065), 2D중 일부 파일은 폴더에 파일 생성
						 */
						if(drawInfo.get(0).get("subasmcheck").equals("T")){
							String filePath = drawInfo.get(0).get("filepath").toString();
							
							/*
							 * filePath명으로 폴더 압축
							 */
							String cmprsTarget = filePath+ ".zip";
							srcFile = new File(uploadDir + fileUtil.SEPERATOR + filePath);
							String targetDirPath = EgovFileTool.createNewDirectory(cmprsPath);
							if (targetDirPath!= "" && srcFile.isDirectory()) {
								File[] fileArr = srcFile.listFiles();
								makeZipFile(fileArr, cmprsPath, cmprsTarget);
							}
						} 
					}
				}
				
				srcFile = null;
				String cmprsTarget = tmpFolderNM+ ".zip";
				srcFile = new File(uploadDirTemp + tmpFolderNM);
				if (srcFile.isDirectory()) {
					File[] fileArr = srcFile.listFiles();
					makeZipFile(fileArr, uploadDirTemp, cmprsTarget);
				}
				retfileDownload(tmpFolderNM, tmpFolderNM, response);
				
				//사용이력 등록
				Map<String, Object>vo = new HashMap<String, Object>();
			  	
				vo.put("refoid", commandMap.get("distoid"));
				vo.put("userid", loginVO.getId());
				vo.put("usetype", "L");
				vo.put("usedes", "도면배포 배포도면 다운로드"); 
				userMngService.insertUseHistory(vo);
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				System.out.println("체크아웃 에러");
			}finally{
				EgovFileTool.deleteDirectory(uploadDirTemp + tmpFolderNM);
				EgovFileTool.deleteFile(uploadDirTemp + tmpFolderNM + ".zip");
			}
		}
		
		//배포담당자 정보 가져오기
		@RequestMapping(value="/draw/select/selecDistUserListSearching.do")
		public String selecDistUserListSearching(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
			
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("teamoid", (String)commandMap.get("teamoid"));
			map.put("humoid", (String)commandMap.get("humoid"));
			map.put("name", (String)commandMap.get("name"));
			map.put("teamname", (String)commandMap.get("teamname"));
			map.put("dir",	(String)commandMap.get("dir"));
			map.put("orderColumn",	(String)commandMap.get("orderColumn"));
			List<Map<String, Object>>  result = drawMngService.selecUserListSearching(map);
			int resultCnt = drawMngService.selecUserListSearchingCnt(map);
			
			model.addAttribute("JSONDataList", result);
			model.addAttribute("resultCnt", resultCnt);
			
			return "yura/draw/select/selecUserListSearching";
		}
		
		/*
		 * 협력업체 도면배포UI
		 */
		@RequestMapping(value="/yura/draw/select/cooperDrawDist.do")
		public String cooperDrawDist(HttpServletRequest request, ModelMap model) {
			return "yura/draw/select/cooperDrawDist";
		}

		/*
		 * 협력업체 도면배포 리스트(협력업체용)
		 */
		@RequestMapping(value="/yura/draw/select/selectCooperDistSearching.do")
		public String selectCooperDistSearching(HttpServletRequest request,
						@RequestParam Map<String, Object> map, ModelMap model) {
			List<Map<String, Object>> result = null;
			int resultCnt = 0;
			try {
				result = drawMngService.selectCooperDistSearching(map);
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				System.out.println("협력업체 도면배포 리스트 에러");
			}
			model.addAttribute("JSONDataList", result);
			model.addAttribute("resultCnt", result.size());
			return "/yura/ccn/select/selectCommonCodeList";
		}
		
		
		//배포도면에서 선택체크된 협력업체 정보 가져오기
		@RequestMapping(value="/draw/select/selectDistDrawComInfo.do")
		public String selectDistDrawComInfo(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
			
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("distoid", (String)commandMap.get("distoid"));
			map.put("modoid", (String)commandMap.get("modoid"));
			List<Map<String, Object>>  result = drawMngService.selectDistDrawComInfo(map);
			
			model.addAttribute("JSONDataList", result);
			model.addAttribute("resultCnt", result.size());
			
			return "yura/draw/select/jsonResultList";
		}
		
		//배포도면에서 선택체크된 배포팀 정보 가져오기
		@RequestMapping(value="/draw/select/selectDistDrawTeamInfo.do")
		public String selectDistDrawTeamInfo(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
			
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("distoid", (String)commandMap.get("distoid"));
			map.put("modoid", (String)commandMap.get("modoid"));
			List<Map<String, Object>>  result = drawMngService.selectDistDrawTeamInfo(map);
			model.addAttribute("JSONDataList", result);
			model.addAttribute("resultCnt", result.size());
			
			return "yura/draw/select/jsonResultList";
		}
		
		/*  
		 * 도면배포현황 리스트(메인화면용)
		 */
		@RequestMapping(value="/draw/select/selectDistStatus.do")
		 public String selectDistStatus(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
			 HashMap<String, Object> map = new HashMap<String, Object>();
			 List<Map<String, Object>>  result = drawMngService.selectDistStatus(map);
			 model.addAttribute("JSONDataList", result);
			 model.addAttribute("resultCnt", result.size());
			
			 return "yura/draw/select/jsonResultList";
		 }
		 
		/*
		 * 도면배포 접수현황 수행통보 메일 발송(3일지난것만 체크하여 발송)
		 */
		@RequestMapping(value="/draw/select/distAcceptSendMail.do")
		public String distAcceptSendMail(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> commandMap) {
			
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("distoid", (String)commandMap.get("oid"));
			map.put("modoid", (String)commandMap.get("modoid"));
			map.put("humid", (String)commandMap.get("humid"));
			
			//도면배포 배포팀 담당자 정보
			List<Map<String, Object>> list;
			try {
				list = drawMngService.selectDistTeamSendEmail(map);
			
				for(Map<String, Object> resultMap : list) {
					String email = (String) resultMap.get("email");
					String name = (String) resultMap.get("humname");
//					String email = "ddang72@yura.co.kr";//test용
					
					String content = "<br> yPLM 시스템에서 도면이 배포되었습니다.";
					content += "<br><br> 사용자 성명:"+ name;
					content += "<br><br> 사용자 이메일:"+ email;
					content += "<br><br><br> ※ 본 메일은 수신 전용입니다.";
	
					//메일 발송
					SndngMailVO sndngMailVO = new SndngMailVO();
			    	sndngMailVO.setDsptchPerson("yPlmMaster");
			    	sndngMailVO.setRecptnPerson(email);
			    	sndngMailVO.setSj("[yPLM] 도면배포 알림");
			    	sndngMailVO.setEmailCn(content);
			    	sndngMailVO.setAtchFileId(""); 
	
			    	sndngMailRegistService.insertSndngMailH(request, sndngMailVO);	
				}
				model.addAttribute("result", "success");
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				System.out.println("도면배포 접수현황 수행통보 메일발송 에러 ");
				model.addAttribute("result", "fail");
			}
			return "yura/draw/select/jsonResultData";
		}
		
		private String makeFolderNM() {
			Date date = new Date();
			String name = "" + date.getTime();
			return name;
		}
		
		private void makeZip(String tmpStoredFilePath, String stordFilePath, String tmpFileName) throws Exception {
			File sourceFile = new File(uploadDir + tmpStoredFilePath);
			File output = new File(uploadDir + tmpFileName);
			if(!sourceFile.isDirectory()) {
				throw new Exception("압축 대상의 파일을 찾을 수가 없습니다.");
			}
			
			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			ZipOutputStream zos = null;
			fos = new FileOutputStream(output);
			bos = new BufferedOutputStream(fos);
			zos = new ZipOutputStream(bos);
			
			zos.setLevel(8);
			zipEntry(sourceFile, stordFilePath, zos); // Zip 파일 생성
			zos.finish(); 
			
			if(zos != null)
				zos.close();
			if(bos != null)
				bos.close();
			if(fos != null)
				fos.close();
		}
		
		/**
	     * 압축
	     * @param sourceFile
	     * @param sourcePath
	     * @param zos
	     * @throws Exception
	     */
	    private static void zipEntry(File sourceFile, String sourcePath, ZipOutputStream zos) throws Exception {
	        // sourceFile 이 디렉토리인 경우 하위 파일 리스트 가져와 재귀호출
	        if (sourceFile.isDirectory()) {
	            if (sourceFile.getName().equalsIgnoreCase(".metadata")) { // .metadata 디렉토리 return
	                return;
	            }
	            File[] fileArray = sourceFile.listFiles(); // sourceFile 의 하위 파일 리스트
	            for (int i = 0; i < fileArray.length; i++) {
	                zipEntry(fileArray[i], sourcePath, zos); // 재귀 호출
	            }
	        } else { // sourcehFile 이 디렉토리가 아닌 경우
	            BufferedInputStream bis = null;
	            try {
	                String sFilePath = sourceFile.getPath();
	                String zipEntryName = sFilePath.substring(sourcePath.length() + 1, sFilePath.length());

	                bis = new BufferedInputStream(new FileInputStream(sourceFile));
	                ZipEntry zentry = new ZipEntry(zipEntryName);
	                zentry.setTime(sourceFile.lastModified());
	                zos.putNextEntry(zentry);

	                byte[] buffer = new byte[1024];
	                int cnt = 0;
	                while ((cnt = bis.read(buffer, 0, 1024)) != -1) {
	                    zos.write(buffer, 0, cnt);
	                }
	                zos.closeEntry();
	            } finally {
	                if (bis != null) {
	                    bis.close();
	                }
	            }
	        }
	    }
	    
		/*
		 * 한글파일 깨짐현상 방지 압축방식
		 */
	    public File makeZipFile(File[] inFile, String zipFilePath, String zipFileName) throws Exception
	    {
	    	File resultFile = new File ( zipFilePath+File.separator+zipFileName );
	    	
	    	ZipArchiveOutputStream zOut = null;
	    	
	    	try {

	    		   zOut = new ZipArchiveOutputStream(new FileOutputStream(resultFile));
	    		   zOut.setEncoding("EUC-KR");//CP852
		   
	    		   for (File f : inFile) {
	    		      InputStream in = new BufferedInputStream(new FileInputStream(f));
	    		      try {
	    		    	  String entryName = f.getName();
	 
	    		    	  /*entryName =  new String(entryName.getBytes("8859_1"), "KSC5601");
	    		    	  ZipArchiveEntry zae = new ZipArchiveEntry( entryName );*/
	     		         zOut.putArchiveEntry(new ZipArchiveEntry( entryName ));
	    		         IOUtils.copy(in, zOut);
	    		         zOut.closeArchiveEntry();
	    		      }
	    		      catch (Exception ex) 
	    		      {
	    		          ex.printStackTrace();
	    		      }
	    		      finally 
	    		      {
	    		    	  IOUtils.closeQuietly(in);
	    		    	 
	    		      }
	    		   }
	    		   zOut.close();
	    		}
	    		catch (Exception ex1) 
	    		{
	    		    ex1.printStackTrace();
	    		}
	    		finally 
	    		{
	    		   IOUtils.closeQuietly(zOut);
	    		}
	    	return resultFile;
	    }
		
		/*
		 * 한글파일 깨짐현상 방지 압축방식
		 */
	    public File makeZipFile2(List<Map<String, Object>> fileList, String zipFilePath, String zipFileName) throws Exception
	    {
	    	File[] inFile = null;
	    	File resultFile = new File ( zipFilePath+File.separator+zipFileName );
	    	ZipArchiveOutputStream zOut = null;
	    	try {
	    		if(fileList != null ){
	    			inFile = new File[fileList.size()];
					for(int j = 0; j < fileList.size(); j++){
						Map fileMap = (Map)fileList.get(j);
						String rfilename = (String)fileMap.get("rfilename");
						String mfilename = (String)fileMap.get("mfilename");
						String filepath = (String)fileMap.get("filepath");
						File sourceFile = new File(uploadDir + filepath + File.separator + mfilename);
						File targetFile = new File(uploadDirTemp + rfilename);
			    		BufferedInputStream in = new BufferedInputStream(new FileInputStream(sourceFile));
			    		OutputStream out = new BufferedOutputStream(new FileOutputStream(targetFile));
			    		FileCopyUtils.copy(in, out);
			    		if(in != null)
			    			in.close();
			    		if(out != null)
			    			out.close();
			    		
						if(targetFile.isFile()){
							inFile[j] = targetFile;
						}
					}
	    		}
	    		zOut = new ZipArchiveOutputStream(new FileOutputStream(resultFile));
	    		zOut.setEncoding("EUC-KR");//CP852
		   
	    		for (File f : inFile) {
	    		      InputStream in = new BufferedInputStream(new FileInputStream(f));
	    		      try {
	    		    	  String entryName = f.getName();
	 
	    		    	  /*entryName =  new String(entryName.getBytes("8859_1"), "KSC5601");
	    		    	  ZipArchiveEntry zae = new ZipArchiveEntry( entryName );*/
	     		         zOut.putArchiveEntry(new ZipArchiveEntry( entryName ));
	    		         IOUtils.copy(in, zOut);
	    		         zOut.closeArchiveEntry();
	    		      }
	    		      catch (Exception ex) 
	    		      {
	    		          ex.printStackTrace();
	    		      }
	    		      finally 
	    		      {
	    		    	  IOUtils.closeQuietly(in);
	    		    	 
	    		      }
	    		  }
	    		  zOut.close();
	    		}
	    		catch (Exception ex1) 
	    		{
	    		    ex1.printStackTrace();
	    		}
	    		finally 
	    		{
	    		   IOUtils.closeQuietly(zOut);
	    		}
	    	return resultFile;
	    }
	    
		/**
		 * 협력업체 도면배포시스템 파일다운로드
		 */
		@RequestMapping(value="/cad/draw/selectDistFileDownload.do")
		public void downloadDistFile(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> commandMap) {
			
			String humid = ((String)commandMap.get("humid"));
			String modoidArry[] = ((String)commandMap.get("modoid")).split(";");
			String tmpFolderNM = ((String)commandMap.get("filepath"));
			String cmprsPath = uploadDirTemp + tmpFolderNM;
			File srcFile;
			try {
				
				//도면별 폴더 압축파일 생성
				for(int i = 0; i < modoidArry.length; i++) {
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("modoid", modoidArry[i]);
					
					List<Map<String ,Object>> drawInfo = drawMngService.retrieveDrawInfo(map);
					if(drawInfo != null && drawInfo.size()>0){
						if(drawInfo.get(0).get("subasmcheck").equals("T")){
							String filePath = drawInfo.get(0).get("filepath").toString();
							
							//선택도면별 파일 filePath명으로 압축파일명생성
							String cmprsTarget = filePath+ ".zip";
							srcFile = new File(uploadDir + fileUtil.SEPERATOR + filePath);
							String targetDirPath = EgovFileTool.createNewDirectory(cmprsPath);
							if (targetDirPath!= "" && srcFile.isDirectory()) {
								File[] fileArr = srcFile.listFiles();
								makeZipFile(fileArr, cmprsPath, cmprsTarget);
							}
						} 
					}
				}
				
				//도면별 압축파일을 묶어서 전체Zip파일 생성
				srcFile = null;
				srcFile = new File(cmprsPath);
				String allCmprsTarget = tmpFolderNM+ ".zip";
				if (srcFile.isDirectory()) {
					File[] fileArr = srcFile.listFiles();
					makeZipFile(fileArr, uploadDirTemp, allCmprsTarget);
				}
				distFileDown(request, response, uploadDirTemp+allCmprsTarget, allCmprsTarget);
				
				//사용이력 등록
				Map<String, Object>vo = new HashMap<String, Object>();
				vo.put("refoid", commandMap.get("distoid"));
				vo.put("userid", humid);
				vo.put("usetype", "L");
				userMngService.insertUseHistory(vo);
				
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				System.out.println("협력업체 도면배포 파일다운로드 에러");
			}finally{
				EgovFileTool.deleteDirectory(cmprsPath);
				EgovFileTool.deleteFile(cmprsPath + ".zip");
			}
		}
		
	    /** 체크아웃/인 처리 */
		@RequestMapping(value="/draw/select/selectCheckOutFlag.do")
		public String selectCheckOutFlag(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
			request.setCharacterEncoding("UTF-8");
			LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
			String result = "";
			String bomoid = (String)request.getParameter("bomoid");
			String[] modIds = (String[])request.getParameterValues("modIds[]");
			String[] modFileIds = (String[])request.getParameterValues("modFileIds[]");
			String[] modFiles = (String[])request.getParameterValues("modFiles[]");
			String procType = (String)request.getParameter("procType");
			HashMap<String, Object> paramMap = new HashMap<String, Object>();
			List<FileVO> listFile = null;
			try {
				paramMap.put("modIds", modIds);
				paramMap.put("modFileIds", modFileIds);
				paramMap.put("modFiles", modFiles);
				paramMap.put("bomoid", bomoid);
				paramMap.put("procType", procType);
				if((procType != null && procType.equals("checkin")) && (modFiles != null && modFiles.length > 0)){
					listFile = FileUploadUtil.uploadFiles2(request, uploadDirTemp, 1000000);
				}
				result = drawMngService.selectCheckInOutProcess(paramMap, listFile);  
			} catch (Exception e) {
				result = "fali";
				LOGGER.error(e.getMessage(), e);
				System.out.println("체크인/아웃 에러 발생");
			}
			model.addAttribute("result", result);
		    return "yura/draw/select/jsonResultData";
		}
			 
		/** 도면배포 다운로드 이력조회 Popup */
		@RequestMapping(value="/draw/select/distDownHistView.do")
		public String distDownHistView(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
			return "yura/draw/select/distDownHistView";
		}
		
		/** 도면배포 다운로드 이력조회 */
		@RequestMapping(value="/draw/select/selecDistDownHist.do")
		public String selecDistDownHist(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
			List<Map<String, Object>> result = drawMngService.selecDistDownHist(commandMap);
			 model.addAttribute("JSONDataList", result);
			 model.addAttribute("resultCnt", result.size());
			return "yura/draw/select/jsonResultList";
		}
		
		public void distFileDown(HttpServletRequest request, HttpServletResponse response, String streFileNm, String orignFileNm) throws Exception {
	    	String downFileName = streFileNm;
	    	String orgFileName = orignFileNm;

	    	File file = new File(downFileName);

	    	if (!file.exists()) {
	    	    throw new FileNotFoundException(downFileName);
	    	}

	    	if (!file.isFile()) {
	    	    throw new FileNotFoundException(downFileName);
	    	}

	    	int fSize = (int)file.length();

	    	if (fSize > 0) {
	    		String mimetype = "application/x-msdownload";
	    		response.setContentType(mimetype);
	    		
	    		setDisposition(orgFileName, request, response);
	    		
	    		response.setContentLength(fSize);

	    		BufferedInputStream in = null;
	    		BufferedOutputStream out = null;

	    		try {
	    		    in = new BufferedInputStream(new FileInputStream(file));
	    		    out = new BufferedOutputStream(response.getOutputStream());

	    		    FileCopyUtils.copy(in, out);
	    		    out.flush();
	    		} catch (Exception ex) {
	    			System.out.println("협력업체 파일 다운로드 에러");
	    		} finally {
	    		    if (in != null) {
	    		    	try {
	    		    		in.close();
	    		    	} catch (Exception ignore) {
	    		    		System.out.println("협력업체 파일 다운로드 에러");
	    		    	}
	    		    }
	    		    if (out != null) {
	    		    	try {
	    		    		out.close();
	    		    	} catch (Exception ignore) {
	    		    		System.out.println("협력업체 파일 다운로드 에러");
	    			    }
	    		    }
	    		}
	    	}
		}
		
		/*
		 * 도면삭제시 참조모듈에서 삭제할 경우 버튼 활성/비활성을 결정용: 참조작업으로 등록했을경우만 버튼 활성용
		 */
		@RequestMapping(value="/draw/select/retrieveDrawRelType.do")
		public String retrieveDrawRelType(@RequestParam HashMap<String, Object> paramMap, ModelMap model) {
			List<Map<String, Object>> list = null;
			try {
				list = drawMngService.selectRelExist(paramMap);
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			model.addAttribute("JSONDataList", list);
			model.addAttribute("resultCnt", list.size());			
			 return "yura/draw/select/jsonResultList";
		}
		
	    /**
	     * EBOM 트리정보
	     *
	     * @param model
	     * @return
	     * @throws Exception
	     */
	    @RequestMapping("/draw/select/selectEbomTreeList.do")
	    public String selectEbomTreeList(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception {
	    	
	    	
	    	HashMap<String,Object> map = new HashMap<String,Object>();
	    	map.put("modoid", (String)commandMap.get("modoid"));
	    	String bomtype = (String)commandMap.get("bomtype");
	    	List<Map<String, Object>> ebomInfo = null;

	    	if((String)commandMap.get("modoid") != null){
	    		//List<Map<String, Object>> rootOidList = drawMngService.selectRootOidList(map);
	    		//if(rootOidList != null && rootOidList.size() > 1){
	    		if(bomtype != null && bomtype.equals("app"))
	    			ebomInfo = drawMngService.selectEbomTreeList(map);
	    		else
	    			ebomInfo = drawMngService.selectRecEbomTreeList(map);
	    		//}else{
	    		//	ebomInfo = drawMngService.selectEbomNotTopPartTreeList(map);
	    		//}
	    	}
	    	//model.addAttribute("ebomInfo", ebomInfo);
	    	//return "yura/draw/select/selectEbomTreeList";
	    	
	    	model.addAttribute("JSONDataList", ebomInfo); 
	    	return "yura/draw/select/jsonResultList";
	    	
	    }
	    
	    /**
	     * BOM 비교 트리정보
	     *
	     * @param model
	     * @return
	     * @throws Exception
	     */
	    @RequestMapping("/draw/select/selectCompareEbomTreeList.do")
	    public String selectCompareEbomTreeList(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception {
	    	HashMap<String,Object> map = new HashMap<String,Object>();
	    	List<Map<String, Object>> firstEbomInfo = null;
	    	List<Map<String, Object>> firstDrawInfo = null;
	    	List<Map<String, Object>> secondEbomInfo = null;
	    	List<Map<String, Object>> secondDrawInfo = null;
	    	List<Map<String, Object>> compareEbomInfo = null;
	    	if((String)commandMap.get("modoid") != null){
	    		map.put("modoid", (String)commandMap.get("modoid"));
	    		firstEbomInfo = drawMngService.selectEbomTreeList(map);
	    		firstDrawInfo = drawMngService.retrieveDrawInfo(map);
	    	}
	    	if((String)commandMap.get("comparedmodoid") != null){
	    		map.put("modoid", (String)commandMap.get("comparedmodoid"));
	    		secondEbomInfo = drawMngService.selectEbomTreeList(map);
	    		secondDrawInfo = drawMngService.retrieveDrawInfo(map);
	    	}
	    	if(firstEbomInfo != null && secondEbomInfo != null){
	    		HashMap<String,Object> paramMap = new HashMap<String,Object>();
	    		paramMap.put("firstmodoid", (String)commandMap.get("modoid"));
	    		paramMap.put("secondmodoid", (String)commandMap.get("comparedmodoid"));
	    		compareEbomInfo = drawMngService.selectSumEbomTreeList(paramMap);
	    	}
	    	model.addAttribute("firstEbomInfo", firstEbomInfo); 
	    	model.addAttribute("secondEbomInfo", secondEbomInfo); 
	    	model.addAttribute("firstDrawInfo", firstDrawInfo); 
	    	model.addAttribute("secondDrawInfo", secondDrawInfo); 
	    	model.addAttribute("compareEbomInfo", compareEbomInfo); 
	    	return "yura/draw/select/selectCompareEbomTreeList"; 
	    }
	    
	    /**
	     * EBOM 도면 추가
	     *
	     * @param model
	     * @return
	     * @throws Exception
	     */
	    @RequestMapping("/draw/insert/InsertEbomInfo.do")
	    public String InsertEbomInfo(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception {
	    	LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
			HashMap<String, Object> map = new HashMap<String, Object>();
			try {
				String bomProcType = (String)commandMap.get("bomProcType");
				map.put("bomProcType", bomProcType);
				//map.put("rootoid", (String)commandMap.get("rootoid"));
				map.put("ebomoid", (String)commandMap.get("ebomoid"));
				map.put("parentoid", (String)commandMap.get("parentoid"));
				map.put("seq", (String)commandMap.get("seq"));
				map.put("modoid", (String)commandMap.get("modoid"));
				map.put("humid", (String)loginVO.getId());
				if(bomProcType != null && bomProcType.equals("bomHighPrtAdd")){
					map.put("p_modoid", (String)commandMap.get("ebomoid"));
					map.put("p_parentoid", (String)commandMap.get("modoid"));
					drawMngService.InsertEbomInfo(map);
					drawMngService.UpdateEbomInfo(map);
				}else if(bomProcType != null && bomProcType.equals("bomPrtAdd")){
					map.put("p_modoid", (String)commandMap.get("modoid"));
					map.put("p_parentoid", (String)commandMap.get("parentoid"));
					drawMngService.InsertEbomInfo(map);
				}else if(bomProcType != null && bomProcType.equals("bomPrtMod")){
					map.put("p_modoid", (String)commandMap.get("modoid"));
					map.put("p_parentoid", (String)commandMap.get("parentoid"));
					drawMngService.UpdateEbomInfo(map);
				}
				drawMngService.call_UpdateEbom(map);
				drawMngService.call_UpdateEbomSeq(map);
				model.addAttribute("result", "SUCCESS");		
				model.addAttribute("resultMsg", egovMessageSource.getMessage("success.common.insert"));		
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				System.out.println("마스터파일 체크 에러");
				model.addAttribute("result", "FAIL");		
				model.addAttribute("resultMsg", egovMessageSource.getMessage("fail.common.insert"));		
			}
			return "yura/draw/select/jsonResultData";
	    }
	    
	    
	    /**
	     * EBOM 도면 삭제
	     *
	     * @param model
	     * @return
	     * @throws Exception
	     */
	    @RequestMapping("/draw/delete/deleteEbomInfo.do")
	    public String deleteEbomInfo(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception {
	    	LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
			HashMap<String, Object> map = new HashMap<String, Object>();
			try {
				//map.put("rootoid", (String)commandMap.get("rootoid"));
				map.put("modoid", (String)commandMap.get("modoid"));
				map.put("parentoid", (String)commandMap.get("parentoid"));
				map.put("p_parentoid", (String)commandMap.get("parentoid"));
				map.put("seq", (String)commandMap.get("seq"));
				int result = drawMngService.deleteEbomInfo(map);
				drawMngService.call_UpdateEbomSeq(map);
				model.addAttribute("result", "SUCCESS");		
				model.addAttribute("resultMsg", egovMessageSource.getMessage("success.common.delete"));		
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				System.out.println("마스터파일 체크 에러");
				model.addAttribute("result", "FAIL");		
				model.addAttribute("resultMsg", egovMessageSource.getMessage("fail.common.delete"));		
			}
			return "yura/draw/select/jsonResultData";
	    }
	    
	    /**
	     * 파트 연결
	     *
	     * @param model
	     * @return
	     * @throws Exception
	     */
	    @RequestMapping("/drawing/update/updateVerprtRelMod.do")
	    public String updateVerprtRelMod(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception {
	    	LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
			HashMap<String, Object> map = new HashMap<String, Object>();
			try {
				map.put("verprtoid", (String)commandMap.get("verprtoid"));
				map.put("modoid", (String)commandMap.get("modoid"));
				int result = drawMngService.updateVerprtRelMod(map);
				model.addAttribute("result", "SUCCESS");		
				model.addAttribute("resultMsg", egovMessageSource.getMessage("success.common.insert"));		
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				model.addAttribute("result", "FAIL");		
				model.addAttribute("resultMsg", egovMessageSource.getMessage("fail.common.insert"));		
			}
			return "yura/draw/select/jsonResultData";
	    }
	    
	    /**
	     * 파트 연결 해제
	     *
	     * @param model
	     * @return
	     * @throws Exception
	     */
	    @RequestMapping("/drawing/delete/deleteVerprtRelMod.do")
	    public String deleteVerprtRelMod(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception {
	    	LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
			HashMap<String, Object> map = new HashMap<String, Object>();
			try {
				map.put("verprtoid", (String)commandMap.get("verprtoid"));
				map.put("modoid", (String)commandMap.get("modoid"));
				int result = drawMngService.deleteVerprtRelMod(map);
				model.addAttribute("result", "SUCCESS");		
				model.addAttribute("resultMsg", egovMessageSource.getMessage("success.common.insert"));		
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				model.addAttribute("result", "FAIL");		
				model.addAttribute("resultMsg", egovMessageSource.getMessage("fail.common.insert"));		
			}
			return "yura/draw/select/jsonResultData";
	    }
	    
	    
	    //도면 BOM 엑셀 다운로드
	    @RequestMapping(value="/yura/draw/select/drawBOMExcelDownload.do")
	    public void drawBOMExcelDownload(ModelMap model, HttpServletRequest request , HttpServletResponse response, @RequestParam HashMap<String, String> paramMap) throws Exception {
	    	request.setCharacterEncoding("UTF-8");
			try{
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("modoid", (String)paramMap.get("modoid"));
				List<Map<String, Object>> ebomInfo = drawMngService.selectEbomTreeList(map);
				HashMap<String, Object> drawMap = new HashMap<String, Object>();
				drawMap.put("ebomInfo",ebomInfo);
				DrawBOMExcelCreate dbec = new DrawBOMExcelCreate(drawMap);
				
				dbec.drawBOMExcelCreateMain(response);
			}catch(Exception e){
				System.out.println("다운로드시 에러발생");
			}

	    }    
	    
	    
		@RequestMapping(value="/draw/select/selectDistReceiveTeamList.do")
		public String selectDistReceiveTeamList(@RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
			 List<Map<String, Object>> result = null;
			 try {
				 result =  drawMngService.selectDistReceiveTeamList(commandMap);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 model.addAttribute("resultCnt", result.size());
			 return "yura/draw/select/jsonResultList";
		}
		
		
		/**
		 * 파트,도면  트리 ROOTOID 적용
		 *
		 * @param 
		 * @return
		 * @throws Exception
		 */
		@RequestMapping(value="/cad/draw/updateBomTreeRootoid.do")
		public String updateBomTreeRootoid(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			String result = "success";
			String msg = "처리 되었습니다.";
			try {
				drawMngService.updateBomTreeRootoid(map);
			} catch (Exception e) {
				e.printStackTrace();
				result = "fail";
				msg = "처리 실패하였습니다.";
			}
			model.addAttribute("result", result);
			return "yura/draw/cad/updateMigResult";
		}
				
		
		
		@RequestMapping(value="/draw/select/selectDistDocDownload.do")
		public void selectDistDocDownload(@RequestParam Map<String, Object> commandMap, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
			String humid = ((String)commandMap.get("humid"));
			String modoid =  (String)commandMap.get("modoid") == null ? "" : ((String)commandMap.get("modoid"));
			String modoidArry[] = modoid.split(";");
			String tmpFolderNM = ((String)commandMap.get("filepath"));
			String cmprsPath = EgovProperties.getProperty("Globals.fileStorePath.doc");
			File srcFile;
			try {
				
				//도면별 폴더 압축파일 생성
//				for(int i = 0; i < modoidArry.length; i++) {
					HashMap<String, Object> map = new HashMap<String, Object>();
//					map.put("modoid", modoidArry[i]);
					System.out.println(commandMap.get("distoid"));
					map.put("distoid", commandMap.get("distoid"));
					List<Map<String ,Object>> docInfo = drawMngService.retrieveDocInfo(map);
					if(docInfo != null && docInfo.size()>0){
						//if(docInfo.get(0).get("subasmcheck").equals("T")){
						tmpFolderNM = docInfo.get(0).get("verdocoid").toString();
						//	String filePath = docInfo.get(0).get("filepath").toString();
						
						for(Map<String, Object> docMap : docInfo) {
							String rFileName = (String) docMap.get("rfilename");
							String mFileName = (String) docMap.get("mfilename");
							
							copyFile2(mFileName, rFileName, cmprsPath + fileUtil.SEPERATOR, cmprsPath + fileUtil.SEPERATOR  + tmpFolderNM + fileUtil.SEPERATOR);
						}
						
						//선택도면별 파일 filePath명으로 압축파일명생성
						String cmprsTarget = tmpFolderNM+ ".zip";
							
//						String docUploadDir = EgovProperties.getProperty("Globals.fileStorePath.doc");	
//						srcFile = new File(docUploadDir + fileUtil.SEPERATOR + tmpFolderNM);
//						String targetDirPath = EgovFileTool.createNewDirectory(cmprsPath + fileUtil.SEPERATOR + tmpFolderNM);
//						if (targetDirPath!= "" && srcFile.isDirectory()) {
//							File[] fileArr = srcFile.listFiles();
//							makeZipFile(fileArr, cmprsPath + fileUtil.SEPERATOR + tmpFolderNM, cmprsTarget);
//						}
						//}  
					}
//				}
				
				//도면별 압축파일을 묶어서 전체Zip파일 생성
				srcFile = null;
				srcFile = new File(cmprsPath + fileUtil.SEPERATOR + tmpFolderNM);
				String allCmprsTarget = tmpFolderNM+ ".zip";
				if (srcFile.isDirectory()) {
					File[] fileArr = srcFile.listFiles();
					makeZipFile(fileArr, uploadDirTemp, allCmprsTarget);
				}
				distFileDown(request, response, uploadDirTemp+allCmprsTarget, allCmprsTarget);
				
				//사용이력 등록
				Map<String, Object>vo = new HashMap<String, Object>();
				vo.put("refoid", commandMap.get("distoid"));
				vo.put("userid", humid);
				vo.put("usetype", "L");
				userMngService.insertUseHistory(vo);
				
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				System.out.println("체크아웃 에러");
			}finally{
				//EgovFileTool.deleteDirectory(cmprsPath + fileUtil.SEPERATOR + tmpFolderNM);
				EgovFileTool.deleteFile(cmprsPath  + fileUtil.SEPERATOR + tmpFolderNM + fileUtil.SEPERATOR + tmpFolderNM + ".zip");
			}
		}
		
		@RequestMapping(value="/cad/draw/updateModuleCheckMigration.do")
		public String updateModuleCheckMigration(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) throws Exception{
			String result = "success";
			try{
				drawMngService.updateModuleCheckMigration(map);
			}catch(Exception e){
				e.printStackTrace();
				result = "fail";
			}
			model.addAttribute("result", result);
			return "yura/part/insert/jsonProcessResult";
		}
		
		
		@RequestMapping(value="/draw/insert/insertVerdochistory.do")
		public String insertVerdochistory(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			String msg = "등록되었습니다.";
			try {
				drawMngService.insertVerdochistory(map);
			} catch (Exception e) {
				e.printStackTrace();
				msg = "등록에 실패하였습니다.";
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
		
		@RequestMapping(value="/draw/select/selectDistDocList.do")
		public String selectDistDocList(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result =  drawMngService.selectDistDocList(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 model.addAttribute("resultCnt", result.size());
			 return "yura/draw/select/jsonResultList";
		}
		
		
		@RequestMapping(value="/draw/update/updateDrawCheckUnlock.do")
		public String updateDrawCheckUnlock(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			String msg = "잠금 해제 처리 되었습니다.";
			try {
				drawMngService.updateDrawCheckUnlock(map);
			} catch (Exception e) {
				e.printStackTrace();
				msg = "해제 처리 실패하였습니다.";
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}

		@RequestMapping(value="/draw/update/updateDrawStatus.do")
		public String updateDrawStatus(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			String result = "success";
			try {
				drawMngService.updateDrawStatus(map);
			} catch (Exception e) {
				e.printStackTrace();
				result = "fail";
			}
			model.addAttribute("result", result);
			return "yura/draw/select/jsonResultData";
		}
		
		//이미지 불러오기
	    @RequestMapping("/yura/draw/select/getImage.do")
		public void getImage(HttpServletRequest request, HttpSession session, @RequestParam HashMap<String, Object> paramMap, HttpServletResponse response){
			if(paramMap.get("filename") != null && !"".equals( String.valueOf (paramMap.get("filename")) ) ){
				String filepath = String.valueOf( paramMap.get("filepath"));
				String filename = String.valueOf( paramMap.get("filename"));
				String realpath = "";
				if(filepath != null && !filepath.equals("")) realpath = uploadDir+filepath+File.separator+filename;
				else realpath = uploadDir+filename;
				BufferedOutputStream out=null;
				InputStream in=null;
				try {
					response.setContentType("image/jpeg");
					response.setHeader("Content-Disposition", "inline;filename="+filename);
					File file=new File(realpath);
					in=new FileInputStream(file);
					out=new BufferedOutputStream(response.getOutputStream());
					int len;
					byte[] buf=new byte[1024];
					while ( (len=in.read(buf)) > 0) {
						out.write(buf,0,len);
					}
					
				}catch(Exception e){
					System.out.println("파일 전송 에러");
				} finally {
					if ( out != null ) try { out.close(); }catch(Exception e){}
					if ( in != null ) try { in.close(); }catch(Exception e){}
				}
			}
		}
	    
	    
	    /**
		 * VIZWeb3D 뷰어
		 */
		@RequestMapping(value="/draw/select/vizWeb3dViewer.do")
		public String vizWeb3dViewer(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
			LoginVO loginVO = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
			String result = "success";
			String modoid = (String)commandMap.get("modoid");
			String rfilename = (String)commandMap.get("rfilename");
			String filename = (String)commandMap.get("filename");
			String filepath = (String)commandMap.get("filepath");
			
			try {
				// vizw 파일 있는지 확인
				String tmpFile = filename.substring(0, filename.lastIndexOf(".")-1);
				String vizwName = tmpFile + ".vizw";
				File vizwFile = new File(vizwDir + vizwName);
				
				if (vizwFile.exists()){
					System.out.println("이미만들어져있음  " + vizwName);	
					
					//// 마무리☆  vizwDir 안에 파일 생긴거 html로 보내면됨!
					result += "|" + vizwName;
				}else{
					HashMap<String, Object> map = new HashMap<String, Object>();
					SimpleDateFormat format1 = new SimpleDateFormat ("yyyyMMddHHmmss");
					String format_time1 = format1.format (System.currentTimeMillis());
					String tmpfolder = File.separator + format_time1;
					
					if(rfilename.indexOf(".CATProduct") > -1){				// 반제품, 완제품
						System.out.println("-----------catproduct");
						map.put("modoid", modoid);
						List<Map<String, Object>> ebomList = drawMngService.selectEbomTreeList(map);				
				   		if(ebomList != null){
				   			List<String> modoidList = new ArrayList<String>();
				   			for(int i=0; i < ebomList.size(); i++){
				   				Map ebomMap = (Map)ebomList.get(i);
				   				modoidList.add((String)ebomMap.get("modoid"));
				   			}
				   			
				   			List<Map<String, Object>> modFilesResult = drawMngService.selectModfilesList(modoidList);
			   				
						    for(int k=0; k<modFilesResult.size(); k++){
						    	Map<String, Object> resMap = (Map)modFilesResult.get(k);
						    	String modfilesOid = (String)resMap.get("oid");
						    	String modfilesName = (String)resMap.get("filename");
						    	String modfilesRname = (String)resMap.get("rfilename");
						    	String fileType = modfilesName.substring(modfilesName.lastIndexOf("."), modfilesName.length());
						    	
								if (fileType.equals(".CATPart") || fileType.equals(".CATProduct")){
									copyFile2(modfilesName, modfilesRname, uploadDir+filepath, vizwTmpDir+tmpfolder);		// [년도폴더] -> [../viz/tmp/YYYYMMDD] 파일 이동
								}
						    }
				   		}
				   		vizwName = transVizwFile(rfilename, filename, vizwTmpDir+tmpfolder, vizwDir);		// [../viz/tmp/YYYYMMDD] -> [../viz/vizw] 파일 이동 및 변환

				   		System.out.println("----product success : " + vizwName);
				   		
				   		//// 마무리☆  vizwDir 안에 파일 생긴거 html로 보내면됨!
				   		result += "|" + vizwName;
				   		
					}else if(rfilename.indexOf(".CATPart") > -1){			// 부품
						System.out.println("------------catpart");
						map.put("rfilename", rfilename);
						map.put("filename", filename);
						map.put("filepath", filepath);
						copyFile2(filename, rfilename, uploadDir+filepath, vizwTmpDir+tmpfolder);			// [년도폴더] -> [../viz/tmp/YYYYMMDD] 파일 이동
						vizwName = transVizwFile(rfilename, filename, vizwTmpDir+tmpfolder, vizwDir);		// [../viz/tmp/YYYYMMDD] -> [../viz/vizw] 파일 이동 및 변환
						
						System.out.println("----part success : " + vizwName);
						
						//// 마무리☆  vizwDir 안에 파일 생긴거 html로 보내면됨!
						result += "|" + vizwName;
						
					}else{
						System.out.println("----------no");
						result = "noCATIA|";
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				result = "fail|";
				System.out.println("3d 뷰어 실패");
			}
			
			System.out.println("result ---> "+ result);
			model.addAttribute("result", result);
			return "yura/draw/select/jsonResultData";
		}
		
		private String transVizwFile(String sourceFileNm, String targetFileNm, String sourceFilePath, String targetFilePath) throws Exception {
			String result = "success";
			
			File targetFolder = new File(targetFilePath);
			if(!targetFolder.exists())
				targetFolder.mkdir();
			
			String tmpfn = targetFileNm.substring(0, targetFileNm.lastIndexOf(".")-1);
			String vizwName = tmpfn + ".vizw";
			
			// VIZCoreTrans.exe -mode web -log 0 -i "D:\web_3d_viewer\1_U_JOINT\00.U_JOINT ASSY.CATProduct" -ovizw "D:\web_3d_viewer\1_U_JOINT\OUTPUT\00.U_JOINT ASSY.vizw"
			String inputFile = EgovWebUtil.filePathBlackList(sourceFilePath+ FileUploadUtil.SEPERATOR + sourceFileNm);
			String outputFile = EgovWebUtil.filePathBlackList(targetFilePath+ FileUploadUtil.SEPERATOR + vizwName);
			
			String command = "cmd.exe /c VIZCoreTrans.exe -mode web -log 0 -i \"" + inputFile + "\" -ovizw \"" + outputFile + "\"";
			System.out.println("command :"+command);
			try {
				Process proc = Runtime.getRuntime().exec(command);
				proc.getErrorStream().close();
				proc.getInputStream().close();
				proc.getOutputStream().close();
				proc.waitFor();							
			} catch (Exception e) {
				result = "Error, cmd";
				e.printStackTrace();
			}
			
			// viz/tmp/ 폴더,파일 삭제
			File folder = new File(sourceFilePath);
			File[] fileList = folder.listFiles();
			if(fileList.length > 0){
				for(int i=0; i<fileList.length; i++){
					fileList[i].delete();				
				}				
			}
			folder.delete();
			
			return vizwName;
		}
		
		@RequestMapping("/yura/mod/VIZWeb3D.do")
		public String VIZWeb3D(HttpServletRequest request, ModelMap model) throws Exception {
			System.out.println("---------- move page!!!");
			return "yura/draw/select/VIZWeb3D";
		}
		
	    /** CAD I/G 관련 쿼리 */
	    @RequestMapping("/cad/draw/insert/selectSearchDrawThumb.do")		// selectDistDocList 베낀거
		public String selectSearchDrawThumb(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.selectSearchDrawThumb(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);		 
			 return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/insert/selectSearchDraw.do")		
		public String selectSearchDraw(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
//			 System.out.println("11111111111");
			 try {
				 result = drawMngService.selectSearchDraw(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);	 
			 return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/insert/selectSearchDraw2.do")		
		public String selectSearchDraw2(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
//			 System.out.println("222222222222");
//			 System.out.println(String.valueOf (map.get("get_dno")));
			 try {
				 result = drawMngService.selectSearchDraw2(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);		 
			 return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/insert/selectCar.do")		
		public String selectCar(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.selectCar(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);		 
			 return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/insert/selectCancledraw.do")		
		public String selectCancledraw(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.selectCancledraw(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 System.out.println(result);
			 model.addAttribute("JSONDataList", result);		 
			 return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/insert/selectMainSearchParent.do")		
		public String selectMainSearchParent(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.selectMainSearchParent(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);		 
			 return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/insert/selectMainSearchChild.do")		
		public String selectMainSearchChild(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.selectMainSearchChild(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 return "yura/draw/select/jsonResultList";
		}
		
	    @RequestMapping("/cad/draw/insert/selectEBOMTreeChild.do")		
		public String selectEBOMTreeChild(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.selectEBOMTreeChild(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/insert/selectMainSearchModfiles.do")		
		public String selectMainSearchModfiles(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.selectMainSearchModfiles(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/insert/selectMainSearchModfilehistory.do")		
		public String selectMainSearchModfilehistory(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.selectMainSearchModfilehistory(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 return "yura/draw/select/jsonResultList";
		}
		
	    @RequestMapping("/cad/draw/select/selectLatestEbomParent.do")		
		public String selectLatestEbomParent(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.selectLatestEbomParent(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);		 
			 return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/select/selectLatestEbomChild.do")		
		public String selectLatestEbomChild(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.selectLatestEbomChild(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 return "yura/draw/select/jsonResultList";
		}
	    	    
	    @RequestMapping("/cad/draw/insert/updateModCheckOut.do")		// updateDrawCheckUnlock 보고베낌
		public String updateModCheckOut(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
	    	String msg = "Success";
			try {
				drawMngService.updateModCheckOut(map);
			} catch(Exception e) {
				e.printStackTrace();
				msg = "Fail";
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
	    
	    @RequestMapping("/cad/draw/insert/updateModCheckOut2.do")		
		public String updateModCheckOut2(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
	        String msg = "Success";
			try {
				drawMngService.updateModCheckOut2(map);
			} catch(Exception e) {
				e.printStackTrace();
				msg = "Fail";
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
	    
	    @RequestMapping("/cad/draw/insert/updateModfilesCheckOut.do")		
		public String updateModfilesCheckOut(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
	        String msg = "Success";
			try {
				drawMngService.updateModfilesCheckOut(map);
			} catch(Exception e) {
				e.printStackTrace();
				msg = "Fail";
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
	    
	    @RequestMapping("/cad/draw/insert/insertModfilehistory2.do")
		public String insertModfilehistory2(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
	    	String msg = "Success";
			try {
				drawMngService.insertModfilehistory2(map);
			} catch (Exception e) {
				e.printStackTrace();
				msg = "Fail";
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
	    
	    @RequestMapping("/cad/draw/insert/updateCancleDraw.do")		
		public String updateCancleDraw(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
	        String msg = "Success";
			try {
				drawMngService.updateCancleDraw(map);
			} catch(Exception e) {
				e.printStackTrace();
				msg = "Fail";
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
	    
	    @RequestMapping("/cad/draw/insert/selectHumCheck.do")		
		public String selectHumCheck(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.selectHumCheck(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/insert/selectModCheckIn.do")		
		public String selectModCheckIn(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.selectModCheckIn(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/select/selectModCheckIn2.do")	
		public String selectModCheckIn2(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.selectModCheckIn2(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);		 
			 return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/insert/selectEbomCheckIn.do")		
		public String selectEbomCheckIn(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.selectEbomCheckIn(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/insert/selectModfilesCheckIn.do")		
		public String selectModfilesCheckIn(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.selectModfilesCheckIn(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/insert/updateNoAddfileCheckIn.do")		
		public String updateNoAddfileCheckIn(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
	        String msg = "Success";
			try {
				drawMngService.updateNoAddfileCheckIn(map);
			} catch(Exception e) {
				e.printStackTrace();
				msg = "Fail";
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
	    
	    @RequestMapping("/cad/draw/insert/updateNoAddfileCheckIn2.do")		
		public String updateNoAddfileCheckIn2(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
	        String msg = "Success";
			try {
				drawMngService.updateNoAddfileCheckIn2(map);
			} catch(Exception e) {
				e.printStackTrace();
				msg = "Fail";
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
	    
	    @RequestMapping("/cad/draw/insert/selectAutoExistInfo.do")		
		public String selectAutoExistInfo(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.selectAutoExistInfo(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/insert/selectRegCatchError.do")		
		public String selectRegCatchError(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.selectRegCatchError(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/insert/selectRegCatchError2.do")		
		public String selectRegCatchError2(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.selectRegCatchError2(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/insert/selectComtecopseq.do")		
		public String selectComtecopseq(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.selectComtecopseq(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/insert/insertAddDataMod.do")		
		public String insertAddDataMod(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
	        String msg = "Success";
			try {
				drawMngService.insertAddDataMod(map);
			} catch(Exception e) {
				e.printStackTrace();
				msg = "Fail";
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
	    
	    @RequestMapping("/cad/draw/insert/updateComtecopseq.do")		
		public String updateComtecopseq(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
	        String msg = "Success";
			try {
				drawMngService.updateComtecopseq(map);
			} catch(Exception e) {
				e.printStackTrace();
				msg = "Fail";
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
	    
	    @RequestMapping("/cad/draw/insert/insertDrawrel.do")		
		public String insertDrawrel(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
	        String msg = "Success";
			try {
				drawMngService.insertDrawrel(map);
			} catch(Exception e) {
				e.printStackTrace();
				msg = "Fail";
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
	    
	    @RequestMapping("/cad/draw/insert/insertAddDataModfiles.do")		
		public String insertAddDataModfiles(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
	        String msg = "Success";
			try {
				drawMngService.insertAddDataModfiles(map);
			} catch(Exception e) {
				e.printStackTrace();
				msg = "Fail";
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
	    
	    @RequestMapping("/cad/draw/insert/insertGetEbomData.do")		
		public String insertGetEbomData(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
	        String msg = "Success";
			try {
				drawMngService.insertGetEbomData(map);
			} catch(Exception e) {
				e.printStackTrace();
				msg = "Fail";
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
	    
	    @RequestMapping("/cad/draw/insert/insertModfilesThumbNail.do")		
		public String insertModfilesThumbNail(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
	        String msg = "Success";
			try {
				drawMngService.insertModfilesThumbNail(map);
			} catch(Exception e) {
				e.printStackTrace();
				msg = "Fail";
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
	    
	    @RequestMapping("/cad/draw/insert/updateAddDataModfiles.do")		
		public String updateAddDataModfiles(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
	        String msg = "Success";
			try {
				drawMngService.updateAddDataModfiles(map);
			} catch(Exception e) {
				e.printStackTrace();
				msg = "Fail";
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
	    
	    @RequestMapping("/cad/draw/insert/selectChkrootoid.do")
		public String selectChkrootoid(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.selectChkrootoid(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/insert/selectChkOutEbomfiles.do")
		public String selectChkOutEbomfiles(HttpServletRequest request, ModelMap model, @RequestParam HashMap<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.selectChkOutEbomfiles(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/insert/selectCCN.do")
		public String selectCCN(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.selectCCN(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/select/selectModMaxVersion.do")		
		public String selectModMaxVersion(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 String strDno = "";
			 try {
				 strDno = (String)map.get("dnolist");
				 System.out.println(strDno);
				 List<String> dnoList = new ArrayList<String>();
				 String[] array = strDno.split(";");
				 for (int idx = 0; idx < array.length; idx++ ) {
					 System.out.println(array[idx]);
					 dnoList.add(array[idx]);				 
				 }
				 result = drawMngService.selectModMaxVersion(dnoList);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/select/selectModDrawrel.do")		
		public String selectModDrawrel(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 String strOid = "";
			 try {
				 strOid = (String)map.get("oidlist");
				 System.out.println(strOid);
				 List<String> oidList = new ArrayList<String>();
				 String[] array = strOid.split(";");
				 for (int idx = 0; idx < array.length; idx++ ) {
					 System.out.println(array[idx]);
					 oidList.add(array[idx]);				 
				 }
				 result = drawMngService.selectModDrawrel(oidList);
				 
				 System.out.println("--------------");
				 System.out.println(result);
				 
				 
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/update/updateCancleStaoid.do")		
		public String updateCancleStaoid(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 String msg = "start";
			 try {
				 msg = drawMngService.updateCancleStaoid(map);
			 } catch(Exception e) {
				 e.printStackTrace();
				 msg = "Exception Fail";
			 }
			 model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
	    
	    @RequestMapping("/cad/draw/update/updateCancleCheckout.do")		
		public String updateCancleCheckout(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 String msg = "Success";
			 String oids = "";
			 try {
				 oids = (String)map.get("oids");
				 System.out.println(oids);
				 List<String> oidList = new ArrayList<String>();
				 String[] array = oids.split(";");
				 for (int idx = 0; idx < array.length; idx++ ) {
					 System.out.println(array[idx]);
					 oidList.add(array[idx]);				 
				 }
				 drawMngService.updateModCancleCheckout(oidList);
				 drawMngService.updateModFilesCancleCheckout(oidList);
			 } catch(Exception e) {
				 e.printStackTrace();
				 msg = "Fail";
			 }
			 model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
	    
	    @RequestMapping("/cad/draw/select/checkMaxVersion.do")		
		public String checkMaxVersion(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
	    	 String msg = "";
			 try {				 
				 msg += "Success::" + drawMngService.checkMaxVersion(map);
			 } catch(Exception e) {
				 msg = "Fail::";
				 e.printStackTrace();
			 }
			 model.addAttribute("resultMsg", msg);
			 return "yura/part/insert/jsonProcessResult";
		}
	    
	    /** CAD I/G 썸네일 이미지 조회 */
	    @RequestMapping("/cad/draw/insert/getThumbImage.do")
		public void getThumbImage(HttpServletRequest request, HttpSession session, @RequestParam HashMap<String, Object> paramMap, HttpServletResponse response) throws Exception{			
			if(paramMap.get("filename") != null && !"".equals( String.valueOf (paramMap.get("filename")) ) ){
				BufferedOutputStream out = null;
				InputStream in = null;
				try {
					String filename = String.valueOf( paramMap.get("filename"));
					String filepath = String.valueOf( paramMap.get("filepath"));
					String rfilePath = uploadDir+filepath+File.separator+filename;
					
					System.out.println(filename);
					System.out.println(filepath);
					System.out.println(rfilePath);
					
					response.setContentType("image/jpeg");
					response.setHeader("Content-Disposition", "inline;filename="+filename);
					File file = new File(rfilePath);
					in = new FileInputStream(file);
					out = new BufferedOutputStream(response.getOutputStream());
					int len;
					byte[] buf = new byte[1024];
					while ( (len = in.read(buf)) > 0) {
						out.write(buf,0,len);
					}
				}catch(Exception e){
					e.printStackTrace();
					System.out.println("파일 전송 에러");
				} finally {
					if ( out != null ) try { out.close(); }catch(Exception e){}
					if ( in != null ) try { in.close(); }catch(Exception e){}
				}
			}
		}
		
		/* 신규등록 (파트만) */
		@RequestMapping(value="/cad/draw/insert/insertNewOnlyPart.do")
		public String insertNewOnlyPart(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{	
			HashMap<String, Object> map = new HashMap<String, Object>();
			String id = "";
			String pwd = "";
			String thumb_dir = "";
			String notypeFname = "";
			String moddata = "";
			String cmd_thumb = "";
			String cmd_thumb2 = "";
			String msg = "";
			
			try{
				id = (String)commandMap.get("id");
				pwd = (String)commandMap.get("pwd");
				thumb_dir = (String)commandMap.get("thumb_dir");
				notypeFname = (String)commandMap.get("notypeFname");
				moddata = (String)commandMap.get("moddata");
				cmd_thumb = (String)commandMap.get("cmd_thumb");
				cmd_thumb2 = (String)commandMap.get("cmd_thumb2");
				
				map.put("id", (String)commandMap.get("id"));
				map.put("pwd", 	EgovFileScrty.encryptPassword(pwd, id));
			}catch (Exception e) {
				e.printStackTrace();
			}
		
			try {
				List<Map<String, Object>> loginCheck = drawMngService.userLoginCheck(map);
				if (loginCheck != null && loginCheck.size()>0) {
					System.out.println("로그인 성공");
					
					// 썸네일 생성
					String[] array = notypeFname.split(";");
					for ( int idx = 0; idx < array.length; idx++ ) {
						String command = "cmd.exe /c " + cmd_thumb + array[idx] + ".CATPart" + cmd_thumb2;
						System.out.println("command :"+command);
						try {
							Process proc = Runtime.getRuntime().exec(command);
							proc.getErrorStream().close();
							proc.getInputStream().close();
							proc.getOutputStream().close();
							proc.waitFor();							
						} catch (Exception e) {
							msg = "Error, cmd : Thumbnail";
							e.printStackTrace();
						}
					}
					
					try {
						msg = drawMngService.insertOnlyPartData(commandMap);
					} catch (Exception e) {
						msg = "Error, ftp";
						LOGGER.error(e.getMessage(), e);
						System.out.println("등록 실패");
					}
				}else{
					System.out.println("로그인 실패");		
				}
			} catch (Exception e) {
				msg = "Error";
				LOGGER.error(e.getMessage(), e);
				System.out.println("에러 발생");
			}
			System.out.println(" ********** result : " + msg);
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
		
		
		/** CAD I/G 신규등록 */
		@RequestMapping(value="/cad/draw/insert/insertNewRegistEBOM.do")
		public String insertNewRegistEBOM(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{	
			HashMap<String, Object> map = new HashMap<String, Object>();
			String id = "";
			String pwd = "";
			String result_file = "";
			String cmd_xml = "";	
			String cmd_thumb = "";
			String msg = "Success";

			try{
				id = (String)commandMap.get("id");
				pwd = (String)commandMap.get("pwd");
				result_file = (String)commandMap.get("path_2");
				cmd_xml = (String)commandMap.get("cmd_xml");	
				cmd_thumb = (String)commandMap.get("cmd_thumb");
				
				map.put("id", (String)commandMap.get("id"));
				map.put("pwd", 	EgovFileScrty.encryptPassword(pwd, id));
				
			}catch (Exception e) {
				e.printStackTrace();
			}
			
		    Map<String, Object> ret_EBOM_Link = new HashMap<String, Object>();
			List<Map<String, Object>> EBOMList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> LinkIDList = new ArrayList<Map<String, Object>>();
			
			try {
				List<Map<String, Object>> loginCheck = drawMngService.userLoginCheck(map);
				if (loginCheck != null && loginCheck.size()>0) {
					System.out.println("로그인 성공");
					
					// xml 생성
					String command = "cmd.exe /c " + cmd_xml;
					System.out.println("command :"+command);
					try {
						Process proc = Runtime.getRuntime().exec(command);
						proc.getErrorStream().close();
						proc.getInputStream().close();
						proc.getOutputStream().close();
						proc.waitFor();							
					} catch (Exception e) {
						msg = "Error, cmd : xml";
						e.printStackTrace();
					}

					// 썸네일 생성
					String command2 = "cmd.exe /c " + cmd_thumb;
					System.out.println("command :"+command2);
					try {
						Process proc = Runtime.getRuntime().exec(command2);
						proc.getErrorStream().close();
						proc.getInputStream().close();
						proc.getOutputStream().close();
						proc.waitFor();							
					} catch (Exception e) {
						msg = "Error, cmd : Thumbnail";
						e.printStackTrace();
					}
					
					try {
						// BOM 트리구조 생성
						EBOMTree eTree = new EBOMTree();
						
						ret_EBOM_Link = (Map<String, Object>) eTree.xmlParsing(result_file);
						EBOMList = (ArrayList<Map<String, Object>>)ret_EBOM_Link.get("al_EBOM");
						LinkIDList = (ArrayList<Map<String, Object>>)ret_EBOM_Link.get("al_Link");
						
						commandMap.put("EBOMList", EBOMList);
						commandMap.put("LinkIDList", LinkIDList);
						
						msg = drawMngService.insertNewRegistEBOM(commandMap);
					} catch (Exception e) {
						msg = "Error, addEbom";
						LOGGER.error(e.getMessage(), e);
						System.out.println("등록 실패");
					}
				}else{
					System.out.println("로그인 실패");		
				}
			} catch (Exception e) {
				msg = "Error";
				LOGGER.error(e.getMessage(), e);
				System.out.println("에러 발생");
			}
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
		
	    
	    /** CAD I/G 도면 개정시 2222 */
		@RequestMapping(value="/cad/draw/insert/checkoutRevision2.do")
		public String checkoutRevision2(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{	
			List<Map<String, Object>> prtMbomMap = new ArrayList<Map<String, Object>>();
			
			System.out.println("------- newRev");
			String msg = "";
			try{
				String putoid = (String)commandMap.get("putoid");
				String putdno = (String)commandMap.get("putdno");
				String putnum = (String)commandMap.get("putnum");
				String putstep = (String)commandMap.get("putstep");
				String puteono = (String)commandMap.get("puteono");
				String putprtpno = (String)commandMap.get("putprtpno");
				String chkhumid = (String)commandMap.get("chkhumid");
				String chkout_path = (String)commandMap.get("chkout_path");
				String root_oid = (String)commandMap.get("root_oid");
				
				System.out.println("---------- get param !!!");
				System.out.println(putoid);
				System.out.println(putdno);
				System.out.println(putnum);
				System.out.println(putstep);
				System.out.println(puteono);
				System.out.println(putprtpno);
				System.out.println(chkhumid);
				System.out.println(chkout_path);
				System.out.println(root_oid);

				String[] oid = putoid.split(";");
				String[] dno = putdno.split(";");
				String[] num = putnum.split(";");
				String[] step = putstep.split(";");
				String[] eono = puteono.split(";");
				String[] prtpno = putprtpno.split(";");
				
				List<Map<String, Object>> map = new ArrayList<Map<String, Object>>();
			    for (int i=0; i<oid.length; i++) {
			    	HashMap<String,Object> tmpMap = new HashMap<String,Object>();
			    	System.out.println(oid[i]);
			    	tmpMap.put("oid", oid[i]);
			    	tmpMap.put("dno", dno[i]);
			    	tmpMap.put("num", num[i]);
			    	tmpMap.put("step", step[i]);
			    	tmpMap.put("eono", eono[i]);
			    	tmpMap.put("prtpno", prtpno[i]);
			    	tmpMap.put("chkhumid", chkhumid);
			    	tmpMap.put("chkout_path", chkout_path);
			    	tmpMap.put("root_oid", root_oid);
			    	map.add(tmpMap);
			    }
			    // prtMbomMap = drawMngService.checkoutRevisionMod2(map);
			    msg = drawMngService.checkoutRevisionMod2(map);
			    /*
			    // 나중에 여기 밑에는 삭제 
			    System.out.println("---- success ----");
			    for(int k=0; k<prtMbomMap.size(); k++){
			    	Map<String, Object> resMap = (Map)prtMbomMap.get(k);
			    	String oldoid = (String)resMap.get("oid");
			    	String newoid = (String)resMap.get("newoid");
			    	String flag = (String)resMap.get("masterflag");
			    	String loginID = (String)resMap.get("chkhumid");
			    	System.out.println("* oldoid: "+oldoid+", newoid: "+newoid+", flag: "+flag+", ID: "+loginID);
			    }
			    
			     ---- success ---- 파트:도면  이 n대1이어서 T가 여러개
				* oldoid: VPR98730006, newoid: VPR00001857, flag: F, ID: admin
				* oldoid: VPR98730009, newoid: VPR00001858, flag: F, ID: admin
				* oldoid: VPR98730001, newoid: VPR00001859, flag: T, ID: admin
				* oldoid: VPR98730002, newoid: VPR00001860, flag: T, ID: admin
				* oldoid: VPR98730005, newoid: VPR00001861, flag: T, ID: admin
				* oldoid: VPR98730007, newoid: VPR00001862, flag: T, ID: admin
			    
			     msg = drawMngService.updateDrawRelPartRevProcess2(prtMbomMap);*/
			     
			} catch (Exception e) {
				msg = "Error";
				LOGGER.error(e.getMessage(), e);
				System.out.println("에러 발생");
			}
			
			System.out.println(msg);
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
	    
	    /** CAD I/G 도면 개정시 */
		@RequestMapping(value="/cad/draw/insert/checkoutRevision.do")
		public String checkoutRevision(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{	
			List<Map<String, Object>> prtMbomMap = new ArrayList<Map<String, Object>>();
			
			System.out.println("------- newRev");
			String msg = "";
			try{
				String putoid = (String)commandMap.get("putoid");
				String putdno = (String)commandMap.get("putdno");
				String putnum = (String)commandMap.get("putnum");
				String putstep = (String)commandMap.get("putstep");
				String chkhumid = (String)commandMap.get("chkhumid");
				String chkout_path = (String)commandMap.get("chkout_path");
				String root_oid = (String)commandMap.get("root_oid");
				
				String[] oid = putoid.split(",");
				String[] dno = putdno.split(",");
				String[] num = putnum.split(",");
				String[] step = putstep.split(",");
				
				List<Map<String, Object>> map = new ArrayList<Map<String, Object>>();
			    for (int i=0; i<oid.length; i++) {
			    	HashMap<String,Object> tmpMap = new HashMap<String,Object>();
			    	System.out.println(oid[i]);
			    	tmpMap.put("oid", oid[i]);
			    	tmpMap.put("dno", dno[i]);
			    	tmpMap.put("num", num[i]);
			    	tmpMap.put("step", step[i]);
			    	tmpMap.put("chkhumid", chkhumid);
			    	tmpMap.put("chkout_path", chkout_path);
			    	tmpMap.put("root_oid", root_oid);
			    	map.add(tmpMap);
			    }
			    prtMbomMap = drawMngService.checkoutRevisionMod(map);
			    
			    // 나중에 여기 밑에는 삭제 
			    System.out.println("---- success ----");
			    for(int k=0; k<prtMbomMap.size(); k++){
			    	Map<String, Object> resMap = (Map)prtMbomMap.get(k);
			    	String oldoid = (String)resMap.get("oid");
			    	String newoid = (String)resMap.get("newoid");
			    	String flag = (String)resMap.get("masterflag");
			    	String loginID = (String)resMap.get("chkhumid");
			    	System.out.println("* oldoid: "+oldoid+", newoid: "+newoid+", flag: "+flag+", ID: "+loginID);
			    }
			    
			    /* ---- success ----
				* oldoid: VPR00000602, newoid: VPR00001154, flag: F, ID: admin
				* oldoid: VPR00000602, newoid: VPR00001155, flag: F, ID: admin
				* oldoid: VPR00000602, newoid: VPR00001156, flag: T, ID: admin*/
			    
			     msg = drawMngService.updateDrawRelPartRevProcess(prtMbomMap);
			    
			} catch (Exception e) {
				msg = "Error";
				LOGGER.error(e.getMessage(), e);
				System.out.println("에러 발생");
			}
			
			System.out.println(msg);
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
	    
		@RequestMapping("/cad/draw/select/selectMainEbomTree.do")		
		public String selectMainEbomTree(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.selectMainEbomTree(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 return "yura/draw/select/jsonResultList";
		}
		
		@RequestMapping("/cad/draw/select/selectPartInfo.do")
		public String selectPartInfo(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.selectPartInfo(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 return "yura/draw/select/jsonResultList";
		}

		/* 체크인 - 추가파일 없을때 */
		@RequestMapping(value="/cad/draw/update/updateCheckInInfo.do")
		public String updateCheckInInfo(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{	
			HashMap<String, Object> map = new HashMap<String, Object>();
			String id = "";
			String pwd = "";
			String cmd_xml = "";
			String cmd_thumb = "";
			String result_file = "";
			String msg = "";
			
			try{
				id = (String)commandMap.get("id");
				pwd = (String)commandMap.get("pwd");
				cmd_xml = (String)commandMap.get("cmd_xml");
				cmd_thumb = (String)commandMap.get("cmd_thumb");
				result_file = (String)commandMap.get("path_2");
				map.put("id", (String)commandMap.get("id"));
				map.put("pwd", 	EgovFileScrty.encryptPassword(pwd, id));
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			Map<String, Object> ret_EBOM_Link = new HashMap<String, Object>();
			List<Map<String, Object>> EBOMList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> LinkIDList = new ArrayList<Map<String, Object>>();
			
			try {
				List<Map<String, Object>> loginCheck = drawMngService.userLoginCheck(map);
				if (loginCheck != null && loginCheck.size()>0) {
					System.out.println("로그인 성공");
					
//					// xml 생성
//					String command = "cmd.exe /c " + cmd_xml;
//					System.out.println("command :"+command);
//					try {
//						Process proc = Runtime.getRuntime().exec(command);
//						proc.getErrorStream().close();
//						proc.getInputStream().close();
//						proc.getOutputStream().close();
//						proc.waitFor();							
//					} catch (Exception e) {
//						msg = "Error, cmd : xml";
//						e.printStackTrace();
//					}

					// 썸네일 생성
					String command2 = "cmd.exe /c " + cmd_thumb;
					System.out.println("command :"+command2);
					try {
						Process proc = Runtime.getRuntime().exec(command2);
						proc.getErrorStream().close();
						proc.getInputStream().close();
						proc.getOutputStream().close();
						proc.waitFor();							
					} catch (Exception e) {
						msg = "Error, cmd : Thumbnail";
						e.printStackTrace();
					}
					
					try {
						// BOM 트리구조 생성
//						EBOMTree eTree = new EBOMTree();
//						ret_EBOM_Link = (Map<String, Object>) eTree.xmlParsing(result_file);
//						EBOMList = (ArrayList<Map<String, Object>>)ret_EBOM_Link.get("al_EBOM");
//						LinkIDList = (ArrayList<Map<String, Object>>)ret_EBOM_Link.get("al_Link");
//						
//						commandMap.put("EBOMList", EBOMList);
//						commandMap.put("LinkIDList", LinkIDList);
						
						msg = drawMngService.updateCheckInInfo(commandMap);
					} catch (Exception e) {
						msg = "Error, insertnotAddData";
						LOGGER.error(e.getMessage(), e);
						System.out.println("등록 실패");
					}
					
				}else{
					System.out.println("로그인 실패");		
				}
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				System.out.println("에러 발생");
			}
			System.out.println(" ********** result : " + msg);
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
		}
		
		@RequestMapping("/cad/draw/select/autoFillModInfo.do")
		public String autoFillModInfo(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.autoFillModInfo(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 return "yura/draw/select/jsonResultList";
		}
		
		@RequestMapping("/cad/draw/select/selectComtecopseq2.do")		
		public String selectComtecopseq2(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.selectComtecopseq2(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 return "yura/draw/select/jsonResultList";
		}
		
		@RequestMapping("/cad/draw/select/selectRegCatchError3.do")		
		public String selectRegCatchError3(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.selectRegCatchError3(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 return "yura/draw/select/jsonResultList";
		}
		
		/** CAD I/G 추가파일 있을때 */
	    @RequestMapping(value="/cad/draw/update/updateCheckInAddEbom.do")
		public String updateCheckInAddEbom(HttpServletRequest request, @RequestParam Map<String, Object> commandMap, ModelMap model) throws Exception{
	    	System.out.println("Add 호출");
			HashMap<String, Object> map = new HashMap<String, Object>();
			String id = "";
			String pwd = "";
			String result_file = "";
			String cmd_xml = "";	
			String cmd_thumb = "";
			String msg = "Success";
			
			try{
				id = (String)commandMap.get("id");
				pwd = (String)commandMap.get("pwd");
				result_file = (String)commandMap.get("path_2");
				cmd_xml = (String)commandMap.get("cmd_xml");	
				cmd_thumb = (String)commandMap.get("cmd_thumb");
				
				map.put("id", (String)commandMap.get("id"));
				map.put("pwd", 	EgovFileScrty.encryptPassword(pwd, id));
				
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			Map<String, Object> ret_EBOM_Link = new HashMap<String, Object>();
			List<Map<String, Object>> EBOMList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> LinkIDList = new ArrayList<Map<String, Object>>();
			
		    try {
		    	List<Map<String, Object>> loginCheck = drawMngService.userLoginCheck(map);
				if (loginCheck != null && loginCheck.size()>0) {
					System.out.println("로그인 성공");

					// xml 생성
					String command = "cmd.exe /c " + cmd_xml;
					System.out.println("command :"+command);
					try {
						Process proc = Runtime.getRuntime().exec(command);
						proc.getErrorStream().close();
						proc.getInputStream().close();
						proc.getOutputStream().close();
						proc.waitFor();							
					} catch (Exception e) {
						msg = "Error, cmd : xml";
						e.printStackTrace();
					}

					// 썸네일 생성
					String command2 = "cmd.exe /c " + cmd_thumb;
					System.out.println("command :"+command2);
					try {
						Process proc = Runtime.getRuntime().exec(command2);
						proc.getErrorStream().close();
						proc.getInputStream().close();
						proc.getOutputStream().close();
						proc.waitFor();							
					} catch (Exception e) {
						msg = "Error, cmd : Thumbnail";
						e.printStackTrace();
					}
					
					try {
						// BOM 트리구조 생성
						EBOMTree eTree = new EBOMTree();
						ret_EBOM_Link = (Map<String, Object>) eTree.xmlParsing(result_file);
						EBOMList = (ArrayList<Map<String, Object>>)ret_EBOM_Link.get("al_EBOM");
						LinkIDList = (ArrayList<Map<String, Object>>)ret_EBOM_Link.get("al_Link");
						
						commandMap.put("EBOMList", EBOMList);
						commandMap.put("LinkIDList", LinkIDList);
						
						msg = drawMngService.insertAddEBOM(commandMap);
					} catch (Exception e) {
						msg = "Error, addEbom";
						LOGGER.error(e.getMessage(), e);
						System.out.println("등록 실패");
					}
				}else{
					System.out.println("로그인 실패");		
				}
			} catch (Exception e) {
				msg = "Error";
				LOGGER.error(e.getMessage(), e);
				System.out.println("에러 발생");
			}
		    System.out.println(" ********** result : " + msg);
			model.addAttribute("resultMsg", msg);
			return "yura/part/insert/jsonProcessResult";
	    }

	    @RequestMapping("/cad/draw/select/selectPnoMaxStaoid.do")		
		public String selectPnoMaxStaoid(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 String strPno = "";
			 try {
				 strPno = (String)map.get("pnolist");
				 List<String> pnoList = new ArrayList<String>();
				 String[] array = strPno.split(",");
				 for (int idx = 0; idx < array.length; idx++ ) {
					 pnoList.add(array[idx]);				 
				 }
				 result = drawMngService.selectPnoMaxStaoid(pnoList);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/select/selectMainSearchModfiles2.do")		
		public String selectMainSearchModfiles2(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
	    	List<Map<String, Object>> result = null;
	    	String strModoid = "";
	    	try {
	    		strModoid = (String)map.get("modlist");
				List<String> modList = new ArrayList<String>();
				String[] array = strModoid.split(",");
				for (int idx = 0; idx < array.length; idx++ ) {
					System.out.println(array[idx]);
					modList.add(array[idx]);				 
				}
				 result = drawMngService.selectMainSearchModfiles2(modList);
	    	} catch(Exception e) {
				e.printStackTrace();
			}
			model.addAttribute("JSONDataList", result);
			return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/select/selectMainSearchModfilehistory2.do")		
		public String selectMainSearchModfilehistory2(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
	    	List<Map<String, Object>> result = null;
	    	String strModoid = "";
	    	try {
	    		strModoid = (String)map.get("modlist");
				List<String> modList = new ArrayList<String>();
				String[] array = strModoid.split(",");
				for (int idx = 0; idx < array.length; idx++ ) {
					modList.add(array[idx]);				 
				}
				 result = drawMngService.selectMainSearchModfilehistory2(modList);
	    	} catch(Exception e) {
				e.printStackTrace();
			}
			model.addAttribute("JSONDataList", result);
			return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/select/selectLatestModInfo.do")		
		public String selectLatestModInfo(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
	    	List<Map<String, Object>> result = null;
	    	String strModoid = "";
	    	try {
	    		strModoid = (String)map.get("modlist");
				List<String> modList = new ArrayList<String>();
				String[] array = strModoid.split(",");
				for (int idx = 0; idx < array.length; idx++ ) {
					modList.add(array[idx]);				 
				}
				 result = drawMngService.selectLatestModInfo(modList);
	    	} catch(Exception e) {
				e.printStackTrace();
			}
			model.addAttribute("JSONDataList", result);
			return "yura/draw/select/jsonResultList";
		}
	    
	    @RequestMapping("/cad/draw/select/testhayan.do")		
		public String testhayan(HttpServletRequest request, ModelMap model, @RequestParam Map<String, Object> map) {
			 List<Map<String, Object>> result = null;
			 try {
				 result = drawMngService.testhayan(map);
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
			 model.addAttribute("JSONDataList", result);
			 return "yura/draw/select/jsonResultList";
		}
	}
