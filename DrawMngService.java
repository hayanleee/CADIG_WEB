package com.yura.draw.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestParam;

import com.yura.common.util.FileVO;
import com.yura.draw.DrawVO;


public interface DrawMngService {
    /**
     *  도면등록
     */
    public Map<String, Object> registertDrawInfo(List<FileVO> fileList, HashMap<String, Object> map) throws Exception;
   
    /**
     * 파트관련 도면체크
     */
    public List<Map<String, Object>> checkPartDrawInfo(DrawVO vo) throws Exception;
    
    /**
     * 도면버전정보 리스트
     */
    public List<Map<String, Object>> retrievePreDrawList(DrawVO vo) throws Exception;

    /**
     * CAD 종류
     */
    public List<Map<String, Object>> selectSftInfo(String iscad) throws Exception;
    
    /**
     * 사용자 정보 조회
     */
	public List<Map<String, Object>> selecUserListSearching(HashMap<String, Object> map) throws Exception;
	
    /**
     * 사용자 정보 count
     */
	public int selecUserListSearchingCnt(HashMap<String, Object> map) throws Exception;
	
	/**
	 * 서브파트 여부 체크
	 */
	public List<Map<String, Object>> checkSubPart(HashMap<String, String> map) throws Exception;
	
	/**
	 * 도면 개정정보 리스트
	 */
	public List<Map<String, Object>>  selectPreDrawList(HashMap<String, String> map) throws Exception;
	
	/**
	 * 배포담당자 리스트
	 */
	public List<Map<String, Object>>  userDistSearching(HashMap<String, String> map) throws Exception;
	
	/**
	 * 협력업체 리스트
	 */
	public List<Map<String, Object>>  comDistSearching(HashMap<String, String> map) throws Exception;
	
	
	/**
	 * 미결재 리스트
	 */
	public List<Map<String, Object>>  retrieveApprovalList(HashMap<String, String> map) throws Exception;
	
	/**
	 * 결재첨부문서 리스트
	 */
	public List<Map<String, Object>>  retrieveDrawDocList(HashMap<String, String> map) throws Exception;
	
	/**
	 * 입력한 개정번호가 PVS테이블에 존재하는 개정번호인지 체크(PVS테이블:개정관리 테이블)
	 */
	public Object retrieveCheckPvs(HashMap<String, Object> map) throws Exception;
	
	/**
	 * 도면정보에 개정번호가 동일한 정보가 있는지 체크
	 */
	public List<Map<String, Object>> retrieveCheckEqualPvs(HashMap<String, Object> map) throws Exception;
	
	/**
	 * 도면정보 결재상태 체크
	 */
	public List<Map<String, Object>> retrieveCheckStatus(HashMap<String, Object> map) throws Exception;
	
    /**
     * 도면indexno
     */
    public int retrieveIndexNo(HashMap<String, Object> map) throws Exception;

	/**
	 * 도면첨부파일 리스트
	 */
	public List<Map<String, Object>> selectModFileList(String[] oidList) throws Exception;

	/**
	 * 도면개정 리스트
	 */
	public List<Map<String, Object>> selectPvsInfo(HashMap<String, Object> map) throws Exception;
	
	/**
	 * 도면상세 정보
	 */
	public List<Map<String, Object>> retrieveDrawInfo(HashMap<String, Object> map) throws Exception;
	
	/**
	 * 도면체크아웃
	 */
	public int updateCheckOut(HttpServletRequest request, HashMap<String, Object> map) throws Exception;
	public int updateModFileCheckOut(HttpServletRequest request, HashMap<String, Object> map) throws Exception;
	
	/**
	 * 도면체크인
	 */
	public int updateCheckIn(HttpServletRequest request, HashMap<String, Object> map) throws Exception;
	
	/**
	 * 도면수정
	 */
	public int updateDrawInfo(HttpServletRequest request, HashMap<String, Object> map) throws Exception;
	
	/**
	 * 도면삭제
	 */
	public int deleteDrawInfo(HttpServletRequest request, HashMap<String, Object> map) throws Exception;
	
	/**
	 * 도면 파일삭제
	 */
	public int deleteDrawFile(HttpServletRequest request, HashMap<String, Object> map) throws Exception;
	
	/**
	 * 도면개정
	 */
	public int retrieve(HttpServletRequest request, HashMap<String, Object> map) throws Exception;
	
    /**
     * 도면첨부파일 저장
     */
    public List<String> registerAttachFile(List<FileVO> fileList, DrawVO vo) throws Exception;

    /**
     * 도면 subassemble 조회
     */
    public List<Map<String, Object>> selectSubAsmList(HttpServletRequest request, HashMap<String, Object> map) throws Exception;

    /**
     * 도면파일추가 등록(단일파일)
     */
    public int uploadAdditionFile2D(HashMap<String, Object> map) throws Exception;

    /**
     * 도면파일추가 등록(멀티파일)
     */
    public int uploadAdditionFile3D(List<FileVO> file, HashMap<String, Object> map) throws Exception;
    
    /**
     * 파트 리스트 조회
     */
    public List<Map<String, Object>> retrievePartSearchList(HashMap<String, Object> map) throws Exception;
    
    /**
     * 도면 리스트 조회
     */
    public List<Map<String, Object>> retrieveDrawSearchList(HashMap<String, Object> map) throws Exception;

    /**
     * 프로젝트 리스트 조회
     */
    public List<Map<String, Object>> retrievePrjectSearchList(HashMap<String, Object> map) throws Exception;
    
	/**
	 * 도면정보 개정생성을 위한 pvsoid 정보
	 */
    public Object retrieveCheckDrawPvs(HashMap<String, Object> map) throws Exception;
    
    public Object retrieveMaxDrawCheck(HashMap<String, Object> map) throws Exception;
    
    
    /**
     * 도면정보 3D파일 폴더SEQ
     */
    public Object retrieveDirSeq(HashMap<String, Object> map) throws Exception;
    
    /**
     * CAD에서 로그인 체크
     */
    public List<Map<String, Object>> userLoginCheck(HashMap<String, Object> map) throws Exception;
    
    /**
     * 문서파일  삭제
     */
    public int deleteDocInfo(HashMap<String, String> map) throws Exception;
    
    /**
	 * 문서선택 
	 */
	public int insertDrawDocRel(HashMap<String, String> map) throws Exception;
	
    /**
     * 도면개정리스트
     */
    public List<Map<String, Object>> retrieveDrawVersionList(HashMap<String, Object> map) throws Exception;
    
    /**
     * 도면과 연결된 도면리스트
     */
    public List<Map<String, Object>> retrieveDrawRelDrawList(HashMap<String, Object> map) throws Exception;
    
    
    /**
     * ERP BOM리스트
     */
    public List<Map<String, Object>> retrieveUnitDrawList(HashMap<String, Object> map) throws Exception;
    
    /**
     * 설변정보 리스트
     */
    public List<Map<String, Object>> retrieveEcPartList(HashMap<String, Object> map) throws Exception;
    
    /**
     * 프로젝트 리스트
     */
    public List<Map<String, Object>> retrieveRelationModuleInfo(HashMap<String, Object> map) throws Exception;
    
    /**
     *  도면등록(CAD I/G)
     */
    public String registertDrawInfoCAD(List<FileVO> fileList, HashMap<String, Object> map) throws Exception;
   
    /**
     * 파트카테고리 리스트(CAD I/G)를 등록한다.
     */
    public List<Map<String, Object>> selectCatPrtList(HashMap<String, Object> map) throws Exception;
    
    /**
     * 엔진카테고리 리스트(CAD I/G)를 등록한다.
     */
    public List<Map<String, Object>> selectCatEngList(HashMap<String, Object> map) throws Exception;
    
    /** 
     * 개정생성
     */
    public Map<String, Object> insertNewVersion(HashMap<String, Object> map) throws Exception;
    
    /** 
     * 3D파일용 Assemble List조회
     */
    public List<Map<String, Object>> retrieveSubAssemblyInfo(HashMap<String, Object> map) throws Exception;
    
    public List<Map<String, Object>> retrieveSubHistAssemblyInfo(HashMap<String, Object> map) throws Exception;
    
    /** 
     * 3D파일용 Assemble FileList 조회
     */
    public List<Map<String, Object>>  retrieveAsmFileInfo(Map<String, Object> map) throws Exception;
    
    /** 
     * 3D파일용 Assemble FileList 등록
     */
    public int registerAsmFileInfo(List<FileVO> fileList, HashMap<String, Object> map) throws Exception;
    
    /** 
     * 마스터 파일 조회
     */
    public List<Map<String, Object>>  retrieveMasterFileInfo(Map<String, Object> map) throws Exception;
    
    /** 
     * 3D파일용 Assemble FileList 삭제
     */
    public int deleteAsmFileInfo(HashMap<String, Object> map) throws Exception;

    public int updateDistAsmFile(List<Map<String, Object>> map) throws Exception;
    
	public List<HashMap<String,String>> selectPrjStageByPrjMain(HashMap<String, String> map) throws Exception;
	
	public int updateDrawMasterFile(HashMap<String, Object> map) throws Exception;
	
	public int updateDrawMasterFileAllCheckDelete(HashMap<String, Object> map) throws Exception;
	
    /************************ 도면 배포 서비스 Start ********************************************/
    public List<Map<String, Object>> selectDrawDistTeam() throws Exception;

	public List<Map<String, Object>> selectDistCooperTbl() throws Exception;

	public List<Map<String, Object>> selectCooperManageList(Map<String, Object> map) throws Exception;

	public int selectCooperManageListCnt() throws Exception;

	public List<Map<String, Object>> selectDrawDistTeamList(Map<String, Object> map) throws Exception;

	public int selectDrawDistTeamListCnt() throws Exception;

	public void insertDistTeamHum(List<Map<String, Object>> list) throws Exception;

	public void deleteDistTeamHum(String oid) throws Exception;

	public String insertDrawingRegistrationMain(Map<String, Object> map) throws Exception;

	public void insertDrawDistCom(Map<String, Object> map) throws Exception;

	public void deleteDrawDistCom(String[] oid) throws Exception;

	public List<Map<String, Object>> selectDistSearching(Map<String, Object> map) throws Exception;

	public int selectDistSearchingCnt(Map<String, Object> map) throws Exception;

	public void deleteCooperManage(String oid, String userid) throws Exception;

	public void updateDrawDistCom(Map<String, Object> map) throws Exception;

	public void insertDistAttachFile(Map<String, Object> map) throws Exception;

	public void registertDistModHistoryInfo(Map<String, Object> distModMap) throws Exception;

	public void registertDistTeamHistoryInfo(Map<String, Object> distModMap) throws Exception;

	public List<Map<String, Object>> selectDistInsideList(String distoid) throws Exception;

	public int selectDistInsideListCnt(String distoid) throws Exception;

	public List<Map<String, Object>> selectDistDrawFileList(String distoid) throws Exception;

	public int selectDistDrawFileListCnt(String distoid) throws Exception;

	public List<Map<String, Object>> selectDistDrawList(String distoid) throws Exception;

	public int selectDistDrawListCnt(String distoid) throws Exception;

	public List<Map<String, Object>> selectSearchTeamList(Map<String, Object> map) throws Exception;

	public int selectSearchTeamListCnt(Map<String, Object> map) throws Exception;

	public List<Map<String, Object>> selectDistcomhisryList(Map<String, Object> map) throws Exception;

	public int selectDistcomhisryListCnt(Map<String, Object> map) throws Exception;

	public void insertDrawDistComHistory(Map<String, Object> map) throws Exception;

	public void insertModhistory(Map<String, Object> map) throws Exception;

	public void insertDrawFile(Map<String, Object> map) throws Exception;

	public void insertDrawFileList(List<FileVO> listFile, Map<String, Object> map) throws Exception;

	public void deleteDistFile(Map<String, Object> map) throws Exception;

	public List<Map<String, Object>> selectCcnUnitList(String parentoid) throws Exception;

	public List<Map<String, Object>> selectTeamcomList() throws Exception;

	public void updateDistHistory(Map<String, Object> map) throws Exception;

	public void deleteDistHistory(Map<String, Object> map) throws  Exception;
	
	public List<Map<String, Object>> selectCooperDistSearching(Map<String, Object> map) throws Exception;

	public List<Map<String, Object>> selectComDistComp(Map<String, Object> map) throws Exception;

	public List<Map<String, Object>> selectDistTeamEmail(Map<String, Object> map) throws Exception;

	public List<Map<String, Object>> selectDistTeamSendEmail(Map<String, Object> map) throws Exception;

	public List<Map<String, Object>> selectDistFileList(HashMap<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectDistDrawComInfo(HashMap<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectDistDrawTeamInfo(HashMap<String, Object> map) throws Exception;
	
	public void sendMailDistDrawDelay(HashMap<String, Object> map) throws Exception;

	public void sendMailDistTeamDrawDelay(HashMap<String, Object> map) throws Exception;
	
	public int registertAsmFileCADInfo(HashMap<String, Object> map) throws Exception;
	
	public int updateDrawFileInfo(Map<String, Object> map) throws Exception;

	public void updateCADLoginCheck(Map<String, Object> map) throws Exception;

	public void sendMailDistTeam(Map<String, Object> resultMap) throws Exception;
    
    public void insertModUseHistory(Map<String, Object> vo) throws Exception;
    
    public List<Map<String, Object>> selectDistStatus(Map<String, Object> map) throws Exception;
    
    public void updateDistAppFlag(Map<String, Object> map) throws Exception;
    
	public void updateDrawDistOid(Map<String, Object> map) throws Exception;
	
    public List<Map<String, Object>> selectAppDistTeamMail(Map<String, Object> map) throws Exception;
    
    public int retrieveCheckExistRev(Map<String, Object> map) throws Exception;
	
    public String selectCheckOutFlag(Map<String, Object> map) throws Exception;
    
    public String selectCheckInOutProcess(Map<String, Object> map, List<FileVO> fileList) throws Exception;
    
    public List<Map<String, Object>> selecDistDownHist(Map<String, Object> map) throws Exception;
    
	public List<Map<String, Object>> selectRelExist(HashMap<String, Object> paramMap) throws Exception;
    
    /** EBOM정보*/
    public List<Map<String, Object>> selectEbomTreeList(HashMap<String,Object> map) throws Exception;
    public List<Map<String, Object>> selectRecEbomTreeList(HashMap<String,Object> map) throws Exception;
    
    public List<Map<String, Object>> selectEbomNotTopPartTreeList(HashMap<String,Object> map) throws Exception;
    public List<Map<String, Object>> selectRootOidList(HashMap<String,Object> map) throws Exception;
    
    /** EBOM 파트 등록*/
	public void InsertEbomInfo(HashMap<String, Object> map) throws Exception;
	/** EBOM 파트 수정*/
	public void UpdateEbomInfo(HashMap<String, Object> map) throws Exception;
	
	public void call_UpdateEbom(HashMap<String, Object> map) throws Exception;
	public void call_UpdateEbomSeq(HashMap<String, Object> map) throws Exception;
	
	/** EBOM 파트 삭제*/
	public int deleteEbomInfo(HashMap<String, Object> map) throws Exception;
	
    public void insertDrawFileInfo(HashMap<String, Object> map) throws Exception;
    
	public void deleteDrawFilesInfo(HashMap<String, Object> map) throws Exception;
	
	public int updateVerprtRelMod(HashMap<String, Object> map) throws Exception;
	
	public int deleteVerprtRelMod(HashMap<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> retrieveEbomDrawVerChkList(HashMap<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectRelEbomInfo(HashMap<String, Object> map) throws Exception;
	
	public String updateCheckInCAD(HashMap<String, Object> map, List<Map<String, Object>> fileList) throws Exception;
	
	public void updateCheckInCADBomUpdate(Map<String, Object> map) throws Exception;
	 
	public List<Map<String, Object>> selectSumEbomTreeList(HashMap<String,Object> map) throws Exception;
	
    /**
     * 도면리스트
     */
    public List<Map<String, Object>> retrieveDrawList(HashMap<String, Object> map) throws Exception;
    
    public void registertEbomInfo(HashMap<String, Object> map) throws Exception;
    

	public List<Map<String, Object>> selectDistReceiveTeamList(Map<String, Object> commandMap) throws Exception;

	public List<Map<String, Object>> retrieveDocInfo(HashMap<String, Object> map) throws Exception;

	public void insertVerdochistory(Map<String, Object> map) throws Exception;

	public List<Map<String, Object>> selectDistDocList(Map<String, Object> map) throws Exception;
	
	public void updateDrawCheckUnlock(Map<String, Object> map) throws Exception;
	
	public void updateBomTreeRootoid(Map<String, Object> map) throws Exception;

	public void updateDrawCheckLock(Map<String, Object> map) throws Exception;
	
	public void updateDrawFileCheckUnlock(Map<String, Object> map) throws Exception;
	
	public void updateDrawStatus(Map<String, Object> map) throws Exception;
	
	public void updateModuleCheckMigration(Map<String, Object> map) throws Exception;

	public void insertEBOMTree(List<Map<String, Object>> map) throws Exception;
	
	public void insertThumbnail(List<Map<String, Object>> map) throws Exception;
	
	public void insertAddEBOMTree(List<Map<String, Object>> map) throws Exception;
	
	/** CAD I/G 관련 쿼리 */
	public List<Map<String, Object>> selectSearchDrawThumb(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectSearchDraw(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectSearchDraw2(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectCar(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectCancledraw(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectMainSearchParent(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectMainSearchChild(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectEBOMTreeChild(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectMainSearchModfiles(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectMainSearchModfilehistory(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectLatestEbomParent(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectLatestEbomChild(Map<String, Object> map) throws Exception;
	
	public void updateModCheckOut(Map<String, Object> map) throws Exception;
	
	public void updateModCheckOut2(Map<String, Object> map) throws Exception;
	
	public void updateModfilesCheckOut(Map<String, Object> map) throws Exception;
	
//	public void updateModCancleCheckOut(Map<String, Object> map) throws Exception;
//	
//	public void updateModfilesCancleCheckOut(Map<String, Object> map) throws Exception;
	
	public void insertModfilehistory2(Map<String, Object> map) throws Exception;

	public void updateCancleDraw(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectHumCheck(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectModCheckIn(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectModCheckIn2(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectEbomCheckIn(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectModfilesCheckIn(Map<String, Object> map) throws Exception;
	
	public void updateNoAddfileCheckIn(Map<String, Object> map) throws Exception;
	
	public void updateNoAddfileCheckIn2(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectAutoExistInfo(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectRegCatchError(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectRegCatchError2(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectComtecopseq(Map<String, Object> map) throws Exception;
	
	public void insertAddDataMod(Map<String, Object> map) throws Exception;
	
	public void updateComtecopseq(Map<String, Object> map) throws Exception;
	
	public void insertDrawrel(Map<String, Object> map) throws Exception;
	
	public void insertAddDataModfiles(Map<String, Object> map) throws Exception;
	
	public void insertGetEbomData(Map<String, Object> map) throws Exception;
	
	public void insertNoAddNewEbom(List<Map<String, Object>> map) throws Exception;
	
	public void insertModfilesThumbNail(Map<String, Object> map) throws Exception;
	
	public void updateAddDataModfiles(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectChkrootoid(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectChkOutEbomfiles(HashMap<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectEbomFileList(HashMap<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectCCN(Map<String, Object> map) throws Exception;
	
	public String insertOnlyPartData(Map<String, Object> map) throws Exception;
	
	//CAD IG에서 이미지 정보 조회
	public List<Map<String, Object>> selectCadigDrawInfo(HashMap<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> checkoutRevisionMod(List<Map<String, Object>> map) throws Exception;
	
	// public List<Map<String, Object>> checkoutRevisionMod2(List<Map<String, Object>> map) throws Exception;
	public String checkoutRevisionMod2(List<Map<String, Object>> map) throws Exception;
	
	public void updateCallBomMain(Map<String, Object> map) throws Exception;
	
	public String procPdmMig(Map<String, Object> map) throws Exception;
	
	public String updateDrawRelPartRevProcess(List<Map<String,Object>> paramList) throws Exception;

	public String updateDrawRelPartRevProcess2(List<Map<String,Object>> paramList) throws Exception;
	
	public List<Map<String, Object>> selectModfilesList(List<String> arr) throws Exception;
	
	public List<Map<String, Object>> selectModMaxVersion(List<String> arr) throws Exception;
	
	public List<Map<String, Object>> selectModDrawrel(List<String> arr) throws Exception;
	
	public String updateCancleStaoid(Map<String, Object> map) throws Exception;
	
	public String checkMaxVersion(Map<String, Object> map) throws Exception;
	
	public void updateModCancleCheckout(List<String> arr) throws Exception;
	
	public void updateModFilesCancleCheckout(List<String> arr) throws Exception;
	
	public List<Map<String, Object>> selectMainEbomTree(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectPartInfo(Map<String, Object> map) throws Exception;
	
	public String updateCheckInInfo(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> autoFillModInfo(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectComtecopseq2(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectRegCatchError3(Map<String, Object> map) throws Exception;
	
	public String insertAddEBOM(Map<String, Object> map) throws Exception;
	
	public String insertNewRegistEBOM(Map<String, Object> map) throws Exception;
	
	public List<Map<String, Object>> selectPnoMaxStaoid(List<String> arr) throws Exception;
	
	public List<Map<String, Object>> selectMainSearchModfiles2(List<String> arr) throws Exception;
	
	public List<Map<String, Object>> selectMainSearchModfilehistory2(List<String> arr) throws Exception;
	
	public List<Map<String, Object>> selectLatestModInfo(List<String> arr) throws Exception;
	
	public List<Map<String, Object>> testhayan(Map<String, Object> map) throws Exception;
	
	public String selectModInfo(Map<String, Object> map) throws Exception;
	
	public String selectMaxModoid(Map<String, Object> map) throws Exception;
}
