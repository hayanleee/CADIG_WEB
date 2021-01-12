package com.yura.draw.service.impl;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.util.FileCopyUtils;
import org.terracotta.agent.repkg.de.schlichtherle.io.FileInputStream;
import org.terracotta.agent.repkg.de.schlichtherle.io.FileOutputStream;

import com.yura.common.util.FileVO;
import com.yura.draw.DrawVO;

import egovframework.com.cmm.EgovWebUtil;
import egovframework.com.cmm.service.impl.EgovComAbstractDAO;
import egovframework.com.utl.fcc.service.EgovFormBasedFileUtil;


@Repository("drawMngDAO")
public class DrawMngDAO extends EgovComAbstractDAO {

    /**
     * 도면정보 등록
     * 
     * @param vo
     * @return
     * @throws Exception
     */
    public Object registertDrawInfo(HashMap<String, Object> map) throws Exception {
    	return insert("DrawMngDAO.registertDrawInfo", map);
    }
    
    /**
     * EBOM 정보 등록
     * 
     * @param vo
     * @return
     * @throws Exception
     */
    public Object registertEbomInfo(HashMap<String, Object> map) throws Exception {
    	return insert("DrawMngDAO.registertEbomInfo", map);
    }
    
    
    /**
     * 도면파일정보 등록
     * 
     * @param vo
     * @return
     * @throws Exception
     */
    public Object registertDrawFileInfo(HashMap<String, Object> map) throws Exception {
    	return insert("DrawMngDAO.registertDrawFileInfo", map);
    }
    
    
    /**
     * 도면정보등록시 사용되는 프로세스
     * 
     * @param vo
     * @return
     * @throws Exception
     */
    public Object regUpdateDrawInfo(HashMap<String, Object> map) throws Exception {
    	return update("DrawMngDAO.regUpdateDrawInfo", map);
    }
    
    /**
     * 도면정보수정
     * 
     * @param vo
     * @return
     * @throws Exception
     */
    public Object updateDrawInfo(HashMap<String, Object> map) throws Exception {
    	return update("DrawMngDAO.updateDrawInfo", map);
    }
    
    /**
     * 도면관련모듈 등록
     * 
     * @param vo
     * @return
     * @throws Exception
     */
    public Object registertDrawRelInfo(HashMap<String, Object> map) throws Exception {
    	return insert("DrawMngDAO.registertDrawRelInfo", map);
    }
    
    /**
     * 파트관련 도면조회
     * 
     * @param vo
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> checkPartDrawInfo(DrawVO vo) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.checkPartDrawInfo", vo);
    }
    
    /**
     * 도면버전정보 조회
     * 
     * @param vo
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> retrievePreDrawList(DrawVO vo) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.retrievePreDrawList", vo);
    }

    /**
     * CAD종류(sft)
     * 
     * @param vo
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> selectSftInfo(String iscad) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectSftInfo", iscad);
    }
    
    @SuppressWarnings("unchecked")
	public List<Map<String, Object>> selecUserListSearching(HashMap<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selecUserListSearching", map);
    }
      
    public int selecUserListSearchingCnt(HashMap<String, Object> map) throws Exception {
         return (Integer)select("DrawMngDAO.selecUserListSearchingCnt", map);
    }
    
    /** 
     * 서브파트 여부 체크
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> checkSubPart(HashMap<String,String> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.checkSubPart", map);
    }

    /** 
     * 도면개정정보 리스트
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> selectPreDrawList(HashMap<String,String> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectPreDrawList", map);
    }
    
    /** 
     * 배포담당자 리스트
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> userDistSearching(HashMap<String,String> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.userDistSearching", map);
    }
    
    /** 
     * 협력업체 리스트
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> comDistSearching(HashMap<String,String> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.comDistSearching", map);
    }
    
    /** 
     * 미결재 리스트
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> retrieveApprovalList(HashMap<String,String> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.retrieveApprovalList", map);
    }
    
    /** 
     * 결재첨부문서 리스트
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> retrieveDrawDocList(HashMap<String,String> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.retrieveDrawDocList", map);
    }
    
    /** 
     * 입력한 개정번호가 PVS테이블에 존재하는 개정번호인지 체크(PVS테이블:개정관리 테이블)
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> retrieveCheckPvs(HashMap<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.retrieveCheckPvs", map);
    }
    
    /** 
     * 도면정보에 개정번호가 동일한 정보가 있는지 체크
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> retrieveCheckEqualPvs(HashMap<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.retrieveCheckEqualPvs", map);
    }
    
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> retrieveMaxDrawCheck(HashMap<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.retrieveMaxDrawCheck", map);
    }
    
    /** 
     * 도면정보 결재상태 체크
     */
    @SuppressWarnings({ "unchecked", "deprecation" })
    public List<Map<String, Object>> retrieveCheckStatus(HashMap<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.retrieveCheckStatus", map);
    }
    
    /** 
     * indexno 얻어오기
     */
    public int retrieveIndexNo(HashMap<String, Object> map) throws Exception {
    	return (Integer)select("DrawMngDAO.retrieveIndexNo", map);
    }
    
    /** 
     * 버젼중 최신버젼의 도면을 업데이트(lastflag='T')
     */
    public int updateDrawLastFlag(HashMap<String, Object> map) throws Exception {
    	return (Integer)update("DrawMngDAO.updateDrawLastFlag", map);
    }

    /**
     * 도면배포 등록
     * 
     * @param 
     * @return
     * @throws Exception
     */
    public Object registertDrawDistInfo(HashMap<String, Object> map) throws Exception {
    	return insert("DrawMngDAO.registertDrawDistInfo", map);
    }
    
    /**
     * 도면배포 배포팀 이력 등록
     * 
     * @return
     * @throws Exception
     */
    public Object registertDistModHistoryInfo(HashMap<String, Object> map) throws Exception {
    	return insert("DrawMngDAO.registertDistModHistoryInfo", map);
    }
    
    /**
     * 도면 배포 협력업체 이력 등록
     * 
     * @return
     * @throws Exception
     */
    public Object registertDistTeamHistoryInfo(HashMap<String, Object> map) throws Exception {
    	return insert("DrawMngDAO.registertDistTeamHistoryInfo", map);
    }
    
    /**
     * 도면배포 배포도면 등록
     * 
     * @return
     * @throws Exception
     */
    public Object registertDistComHistoryInfo(HashMap<String, Object> map) throws Exception {
    	return insert("DrawMngDAO.registertDistComHistoryInfo", map);
    }
    
    /**
     * 도면 첨부등록
     * 
     * @return
     * @throws Exception
     */
    public Object registerAttachFile(HashMap<String,String> map) throws Exception {
    	return insert("DrawMngDAO.registerAttachFile", map);
    }
    
    /**
     * 도면 첨부파일 리스트
     * 
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> selectModFileList(HashMap<String,String> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectModAttachFile", map);
    }
    
    /**
     * 도면개정 리스트
     * 
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> selectPvsInfo(HashMap<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectPvsInfo", map);
    }
    
    /**
     * 도면상세정보
     * 
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> retrieveDrawInfo(HashMap<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.retrieveDrawInfo", map);
    }

/*   
    *//**
     *   AA: 조건(pno,pvsname)으로 단위파트존재 확인
     * 
     * @return
     * @throws Exception
     *//*
    public List<Map<String, Object>> selectUnitDrawingPrt(HashMap<String,String> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectUnitDrawingPrt", map);
    }

    *//**
     * @return
     * @throws Exception
     *//*
    public List<Map<String, Object>> selectUnitDrawingPrt2(HashMap<String,String> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectUnitDrawingPrt2", map);
    }
    
    *//**
     * @return
     * @throws Exception
     *//*
    public List<Map<String, Object>> selectDrawingPrt2(HashMap<String,String> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectDrawingPrt2", map);
    }
    
    *//**
     * @return
     * @throws Exception
     *//*
    public Object registerXUnitPart(HashMap<String,String> map) throws Exception {
    	return insert("DrawMngDAO.registerXUnitPart", map);
    }

    *//**
     * @return
     * @throws Exception
     *//*
    public Object registerXUnitVerPart(HashMap<String,String> map) throws Exception {
    	return insert("DrawMngDAO.registerXUnitVerPart", map);
    }
    
    *//**
     * AC: 단위도면 정보 MOD 테이블에 등록
     * @return
     * @throws Exception
     *//*
    public Object registerDrawing(HashMap<String,String> map) throws Exception {
    	return insert("DrawMngDAO.registerDrawing", map);
    }

    *//**
     * AD 단위도면 정보 PSM 테이블에 등록, 단위도면 정보와 부모파트 정보를 연결시켜주는 PSM 테이블에 삽입
     * @return
     * @throws Exception
     *//*
    public Object registerXunitPsm(HashMap<String,String> map) throws Exception {
    	return insert("DrawMngDAO.registerXunitPsm", map);
    }
    
    *//**
     *  AE 해당 단위도면의 파트정보(verprt)를 최신버젼은 'T'로 나머지는 'F'로 변경
     * @return
     * @throws Exception
     *//*
    public Object registerPrtMaxVersion(HashMap<String,String> map) throws Exception {
    	return insert("DrawMngDAO.registerPrtMaxVersion", map);
    }

    *//**
     *  AF 해당 단위도면정보(mod)를 최신버젼은 'T'로 나머지는 'F'로 변경
     * @return
     * @throws Exception
     *//*
    public Object registerUnitDrawMaxVersion(HashMap<String,String> map) throws Exception {
    	return insert("DrawMngDAO.registerUnitDrawMaxVersion", map);
    }

    *//**
     *  AG 도면 정보 DRAWREL 테이블에 등록.. 인데 단위도면은 DRAWREL과 관계없어서 나중에 제외할것임
     * @return
     * @throws Exception
     *//*
    public Object registerDrawRelationInfo(HashMap<String,String> map) throws Exception {
    	return insert("DrawMngDAO.registerDrawRelationInfo", map);
    }
*/ 
    
    /**
     * 체크아웃
     * @return
     * @throws Exception
     */
    public int updateCheckOut(HashMap<String, Object> map) throws Exception {
    	return (Integer)update("DrawMngDAO.updateCheckOut", map);
    }
    
    public int updateModFileCheckOut(HashMap<String, Object> map) throws Exception {
    	return (Integer)update("DrawMngDAO.updateModFileCheckOut", map);
    }
    
    /**
     * 체크인
     * @return
     * @throws Exception
     */
    public int updateCheckIn(HashMap<String, Object> map) throws Exception {
    	return (Integer)update("DrawMngDAO.updateCheckIn", map);
    }
    
    /**
     * 3D 도면 AssembleList
     * 
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> selectSubAsmList(HashMap<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectSubAsmList", map);
    }
    
    public List<Map<String, Object>> selectSubHistAsmList(HashMap<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectSubHistAsmList", map);
    }
    
    
    /**
     * 3D 도면 파일리스트
     * 
     * @return
     * @throws Exception
     */
	public List<Map<String, Object>>  retrieveAsmFileInfo(Map<String, Object> map) throws Exception{
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.retrieveAsmFileInfo", map);
    }
    
	/**
	 * 개정생성시 3D 도면복사등록 파일리스트
	 * 
	 * @return
	 * @throws Exception
	 */
	public Object registerAsmFileCopyInfo(HashMap<String, Object> map) throws Exception{
		return insert("DrawMngDAO.registerAsmFileCopyInfo", map);
	}
	
	public int updateDistAsmFile(List<Map<String, Object>> map) throws Exception{
    	return (Integer)update("DrawMngDAO.updateDistAsmFile", map);
    }
    
    /**
     * 
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> selectRelExist(HashMap<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectRelExist", map);
    }
    
    /**
     * 
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> selectDrawInfoBeforeDelete(HashMap<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectDrawInfoBeforeDelete", map);
    }
    
    /**
     * 
     * @return
     * @throws Exception
     */
    public int deleteDrawFileInfo(HashMap<String, Object> map) throws Exception {
    	return (Integer)update("DrawMngDAO.deleteDrawInfoOfVerprtoid", map);
    }
    
    /**
     * 
     * @return
     * @throws Exception
     */
    public int updateDrawComMaxVersion(HashMap<String, Object> map) throws Exception {
    	return (Integer)update("DrawMngDAO.updateDrawComMaxVersion", map);
    }
    
    /**
     * 
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> selectOtherDrawCnt(HashMap<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectOtherDrawCnt", map);
    }
    
    /**
     * 
     * @return
     * @throws Exception
     */
    public int deletePSM(HashMap<String, Object> map) throws Exception {
    	return (Integer)delete("DrawMngDAO.deletePSM", map);
    }
    
    /**
     * 
     * @return
     * @throws Exception
     */
    public int deleteRelOwn(HashMap<String, Object> map) throws Exception {
    	return (Integer)update("DrawMngDAO.deleteRelOwn", map);
    }
    
    /**
     * 
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> selectRelexistother(Map<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectRelexistother", map);
    }
    
    /**
     * 
     * @return
     * @throws Exception
     */
    public int updatePrjSta(HashMap<String, Object> map) throws Exception {
    	return (Integer)update("DrawMngDAO.updatePrjSta", map);
    }
    
    /**
     * 
     * @return
     * @throws Exception
     */
    public int updatePrjStageDel(HashMap<String, Object> map) throws Exception {
    	return (Integer)update("DrawMngDAO.updatePrjStageDel", map);
    }
    
    /**
     * 도면조회에서 파일추가시 파일정보 수정
     * @return
     * @throws Exception
     */
    public int updateDrawFileInfo(Map<String, Object> map) throws Exception {
    	return (Integer)update("DrawMngDAO.updateDrawFileInfo", map);
    }
    
    /**
     * 도면파일 삭제
     * @return
     * @throws Exception
     */
    public int deleteDrawFile(HashMap<String, Object> map) throws Exception {
    	return (Integer)delete("DrawMngDAO.deleteDrawFile", map);
    }
    
    /**
     * 도면에 문서연계 에서 선택문서 연결정보 삭제시 프로세스
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> selectDrawDocRefInfo(HashMap<String, String> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectDrawDocRefInfo", map);
    }

    
    /**
     * 도면 관련 문서정보 삭제
     * @return
     * @throws Exception
     */
    public int deleteDrawDocInfo(HashMap<String, String> map) throws Exception {
    	return (Integer)delete("DrawMngDAO.deleteDrawDocInfo", map);
    }
    
    /**
     * 도면 관련 문서 정보 수정
     * @return
     * @throws Exception
     */
    public int updateDrawDocRel(HashMap<String, String> map) throws Exception {
    	return (Integer)delete("DrawMngDAO.updateDrawDocRel", map);
    }
    
    
    /**
     * 도면개정리스트
     */
    public List<Map<String, Object>> retrieveDrawVersionList(HashMap<String, Object> map) throws Exception{
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.retrieveDrawVersionList", map);
    }
    
    /**
     * 도면과 연결된 도면리스트
     */
    public List<Map<String, Object>> retrieveDrawRelDrawList(HashMap<String, Object> map) throws Exception{
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.retrieveDrawRelDrawList", map);
    }
    
    
    /**
     * ERP BOM리스트
     */
    public List<Map<String, Object>> retrieveUnitDrawList(HashMap<String, Object> map) throws Exception{
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.retrieveUnitDrawList", map);
    }
    
    /**
     * 설변정보 리스트
     */
    public List<Map<String, Object>> retrieveEcPartList(HashMap<String, Object> map) throws Exception{
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.retrieveEcPartList", map);
    }

    /**
     * 도면개정생성
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> insertNewDrawVersionFileInfo(HashMap<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.insertNewDrawVersionFileInfo", map);
    }

    /**
     * 도면연관 프로젝트 정보 가져오는 프로세스
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> selectRelationModuleOid(HashMap<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectRelationModuleOid", map);
    }

    /**
     * 도면연관 프로젝트 정보 가져오는 프로세스
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> selectProjectModuleInfo(HashMap<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectProjectModuleInfo", map);
    }
    
    /**
     * 도면 등록 프로세스에서 프로젝트 모듈에서 호출된 등록일 경우 등록한다
     * @return
     * @throws Exception
     */
    public Object insertDrawRelationInfoRef(HashMap<String, Object> map) throws Exception {
    	return insert("DrawMngDAO.insertDrawRelationInfoRef", map);
    }

    /**
     * 도면 등록 프로세스에서 프로젝트 모듈에서 호출된 등록일 경우 등록한다
     * @return
     * @throws Exception
     */
    public Object insertDrawRelationInfoDocRef(HashMap<String, Object> map) throws Exception {
    	return insert("DrawMngDAO.insertDrawRelationInfoDocRef", map);
    }
    
    /**
     * 폴더형태로 파일이 업로드된경우 하위파일리스트 등록
     * @return
     * @throws Exception
     */
    public Object registerAsmFileInfo(HashMap<String, Object> map) throws Exception{
    	return insert("DrawMngDAO.registerAsmFileInfo", map);
    }
    
    /**
     * 폴더형태로 파일이 업로드된경우 파일리스트 삭제
     * @return
     * @throws Exception
     */
    public Object deleteAsmFileInfo(HashMap<String, Object> map) throws Exception{
    	return delete("DrawMngDAO.deleteAsmFileInfo", map);
    }
    
    /**
     * 멀티도면일 경우 마스터파일을 별도로 업데이트
     * @return
     * @throws Exception
     */
    public int updateDrawMasterFile(HashMap<String, Object> map) throws Exception{
    	return update("DrawMngDAO.updateDrawMasterFile", map);
    }
    
    public int updateDrawMasterFileAllCheckDelete(HashMap<String, Object> map) throws Exception{
    	return update("DrawMngDAO.updateDrawMasterFileAllCheckDelete", map);
    }
    
    
    ///////////////////////////////////CAD I/G 프로세스 //////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 파트 리스트(CAD I/G용)
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> retrievePartSearchList(HashMap<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.retrievePartSearchList", map);
    }
    
    /**
     * 도면 리스트(CAD I/G용)
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> retrieveDrawSearchList(HashMap<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.retrieveDrawSearchList", map);
    }
    
    /**
     * 프로젝트 리스트(CAD I/G용)
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> retrievePrjectSearchList(HashMap<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.retrieveProjectSearchList", map);
    }

    /**
     * 도면폴더SEQ
     * @return
     * @throws Exception
     */
    public Object retrieveDirSeq(HashMap<String, Object> map) throws Exception {
    	if(map.get("verprtoid") != null && !map.get("verprtoid").equals(""))
    		return getSqlMapClientTemplate().queryForObject("DrawMngDAO.retrieveDirSeq", map);
    	else
    		return getSqlMapClientTemplate().queryForObject("DrawMngDAO.retrieveDirSeqMod", map);
    }

    /**
     * 로그인체크(CAD I/G용)
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> userLoginCheck(HashMap<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.userLoginCheck", map);
    }
    
    /**
     * 파트카테고리 리스트(CAD I/G용)
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> selectCatPrtList(HashMap<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectCatPrtList", map);
    }
    
    /**
     * 엔진카테고리 리스트(CAD I/G용)
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> selectCatEngList(HashMap<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectCatEngList", map);
    }

	public List<Map<String, Object>> selectDrawDistTeam() {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectDistTeamChoice", null);
	}

	public List<Map<String, Object>> selectDistCooperTbl() {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectDistCooperTbl", null);
	}

	public List<Map<String, Object>> selectCooperManageList(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectCooperManageList", map);
	}

	public int selectCooperManageListCnt() {
		return (java.lang.Integer) select("DrawMngDAO.selectCooperManageListCnt", null);
	}

	public List<Map<String, Object>> selectDrawDistTeamList(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectDrawDistTeamList", map);
	}

	public int selectDrawDistTeamListCnt() {
		return (Integer) select("DrawMngDAO.selectDrawDistTeamListCnt", null);
	}

	public void insertDistTeamHum(List<Map<String, Object>> list) {
		for(Map<String, Object> map : list) 
			insert("DrawMngDAO.insertDistTeamHum", map);
	}

	public void deleteDistTeamHum(String[] oid) {
		for(String data : oid)
			delete("DrawMngDAO.deleteDistTeamHum", data);
	}

	public void insertDrawingRegistrationMain(Map<String, Object> map) {
		insert("DrawMngDAO.insertDist", map);
	}

	public void insertDrawDistCom(Map<String, Object> map) {
		insert("DrawMngDAO.insertDrawDistCom", map);
	}

	public void deleteDrawDistCom(String[] oid) {
		for(String data : oid)
			delete("DrawMngDAO.deleteDrawDistCom", data);
	}

	public List<Map<String, Object>> selectDistSearching(Map<String, Object> map) throws Exception {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectDistSearching", map);
	}

	public int selectDistSearchingCnt(Map<String, Object> map) throws SQLException {
		return (Integer) select("DrawMngDAO.selectDistSearchingCnt", map);
	}

	public void deleteCooperManage(String oid) {
		delete("DrawMngDAO.deleteCooperManage", oid);
	}

	public void updateDrawDistCom(Map<String, Object> map) {
		update("DrawMngDAO.updateDrawDistCom", map);
	}

	public void insertDistAttachFile(Map<String, Object> map) {
		insert("DrawMngDAO.insertDistAttachFile", map);
	}

	public void registertDistModHistoryInfo(Map<String, Object> distModMap) {
		insert("DrawMngDAO.registertDistModHistoryInfo", distModMap);
	}

	public List<Map<String, Object>> selectDistInsideList(String distoid) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectDistInsideList", distoid);
	}

	public int selectDistInsideListCnt(String distoid) {
		return (Integer) select("DrawMngDAO.selectDistInsideListCnt", distoid);
	}

	public List<Map<String, Object>> selectDistDrawFileList(String distoid) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectDistDrawFileList", distoid);
	}

	public int selectDistDrawFileListCnt(String distoid) {
		return (Integer) select("DrawMngDAO.selectDistDrawFileListCnt", distoid);
	}

	public List<Map<String, Object>> selectDistDrawList(String distoid) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectDistDrawingList", distoid);
	}

	public int selectDistDrawListCnt(String distoid) {
		return (Integer) select("DrawMngDAO.selectDistDrawListCnt", distoid);
	}

	public List<Map<String, Object>> selectSearchTeamList(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectSearchTeamList", map);
	}

	public int selectSearchTeamListCnt(Map<String, Object> map) {
		return (Integer) select("DrawMngDAO.selectSearchTeamListCnt", map);
	}

	public List<Map<String, Object>> selectDistcomhisryList(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectDistcomhisryList", map);
	}

	public int selectDistcomhisryListCnt(Map<String, Object> map) {
		return (Integer) select("DrawMngDAO.selectDistcomhisryListCnt", map);
	}

	public void insertDrawDistComHistory(Map<String, Object> map) {
		insert("DrawMngDAO.insertDistcomhistory", map);
	}

	public void insertModhistory(Map<String, Object> map) {
		insert("DrawMngDAO.insertModhistory", map);
	}

	public void insertDrawFile(Map<String, Object> map) {
		insert("DrawMngDAO.insertDrawFile", map);
	}

	public void insertDrawFileList(Map<String, Object> map) {
		insert("DrawMngDAO.insertDrawFile", map);
	}

	public void deleteDistFile(Map<String, Object> map) {
		delete("DrawMngDAO.deleteDistFile", map);
	}

	public List<Map<String, Object>> selectCcnUnitList(String parentoid) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectCcnUnitList", parentoid);
	}

	public List<Map<String, Object>> selectTeamcomList() {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectTeamcomList", null);
	}

	public void updateDistHistory(Map<String, Object> map) {
		update("DrawMngDAO.updateDistHistory", map);
	}

	public void deleteDISTMODHistory(Map<String, Object> map) {
		delete("DrawMngDAO.deleteDISTMODHistory", map);
	}

	public void deleteDISTTEAMHistory(Map<String, Object> map) {
		delete("DrawMngDAO.deleteDISTTEAMHistory", map);
	}

	public void deleteDISTFILEHistory(Map<String, Object> map) {
		delete("DrawMngDAO.deleteDISTFILEHistory", map);
	}

	public void deleteDISTHistory(Map<String, Object> map) {
		delete("DrawMngDAO.deleteDISTHistory", map);
	}

	public void selectCommonKey(String name) {
		select("DrawMngDAO.selectCommonKey", name);
	}

	public void updateDistDraw(String distoid) {
		update("DrawMngDAO.updateDistDraw", distoid);
	}

	public int selectDisthumid(Map<String, Object> distoid) {
		return (Integer) select("DrawMngDAO.selectDisthumid", distoid);
	}
	
	public void call_UpdateEbom(HashMap<String, Object> map) throws Exception {
		getSqlMapClientTemplate().queryForObject("DrawMngDAO.call_UpdateEbom", map);
	}
	
	public void call_UpdateEbomSeq(HashMap<String, Object> map) throws Exception {
		getSqlMapClientTemplate().queryForObject("DrawMngDAO.call_UpdateEbomSeq", map);
	}
	
	public List<Map<String, Object>> selectCooperDistSearching(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectCooperDistSearching", map);
	}
	
	public List<Map<String, Object>> selectComDistComp(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectComDistComp", map);
	}

	public List<Map<String, Object>> selectDistTeamEmail(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectDistTeamEmailInfo", map);
	}
	
	public List<Map<String, Object>> selectDistTeamSendEmail(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectDelayDistTeamSendMail", map);
	}

	public List<Map<String, Object>> selectDistFileList(HashMap<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectDistFileList", map);
	}
	
	public List<Map<String, Object>> selectDistDrawComInfo(HashMap<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectDistDrawComInfo", map);
	}
	
	public List<Map<String, Object>> selectDistDrawTeamInfo(HashMap<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectDistDrawTeamInfo", map);
	}
	
	public List<Map<String, Object>> selectDelayDistComInfo(HashMap<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectDelayDistComInfo", map);
	}
	
	public void updateCADLoginCheck(Map<String, Object> map) {
		update("DrawMngDAO.updateCADLoginCheck", map);
	}

    public void insertModUseHistory(Map<String, Object> map) throws Exception {
    	insert("DrawMngDAO.insertModUseHistory", map);
    }
    
    public List<Map<String, Object>> selectDistStatus(Map<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>) list("DrawMngDAO.selectDistStatus", map);
    }
    
    public void updateDistAppFlag(Map<String, Object> map) {
    	update("DrawMngDAO.updateDistAppFlag", map);
    }
    
	public void updateDrawDistOid(Map<String, Object> map) {
		update("DrawMngDAO.updateDrawDistOid", map);
	}
	
    public List<Map<String, Object>> selectAppDistTeamMail(Map<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>) list("DrawMngDAO.selectAppDistTeamMail", map);
    }
    
    /** 동일개정정보로 존재하는지 값체크 */
    public int retrieveCheckExistRev(Map<String,Object> map) throws Exception {
    	return (Integer)select("DrawMngDAO.retrieveCheckExistRev", map);
    }
    
    /** 체크아웃/인 여부 확인 */
    public String selectCheckOutFlag(Map<String,Object> map) throws Exception {
    	return (String)select("DrawMngDAO.selectCheckOutFlag", map);
    }
    
    /** 프로젝트 작업산출물 삭제 */
    public int deletePrjApprove(Map<String,Object> map) throws Exception {
    	return delete("DrawMngDAO.deletePrjApprove", map);
    }
        
    /** 도면배포 파일 다운로드 이력조회 */
    public List<Map<String, Object>> selecDistDownHist(Map<String,Object> map) throws Exception {
    	return  (List<Map<String, Object>>) list("DrawMngDAO.selecDistDownHist", map);
    }
    
    /** 마스터파일 조회 */
    public List<Map<String, Object>> retrieveMasterFileInfo(Map<String, Object> map) throws Exception {
      	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.retrieveMasterFileInfo", map);
      	 
    }
    
    /** EBOM정보*/
    public List<Map<String, Object>> selectEbomTreeList(HashMap<String,Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectEbomTreeList", map);
    }
    
    public List<Map<String, Object>> selectRecEbomTreeList(HashMap<String,Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectRecEbomTreeList", map);
    }
    
	public List<Map<String, Object>> selectEbomNotTopPartTreeList(HashMap<String,Object> map) throws Exception {
		return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectEbomNotTopPartTreeList", map);
	}
	
	public List<Map<String, Object>> selectRootOidList(HashMap<String,Object> map) throws Exception {
		return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectRootOidList", map);
	}
    /** EBOM 파트 등록*/
    public Object InsertEbomInfo(HashMap<String, Object> map) throws Exception {
    	return insert("DrawMngDAO.InsertEbomInfo", map);
    }
    
    /** EBOM 파트 수정*/
    public Object UpdateEbomInfo(HashMap<String, Object> map) throws Exception {
    	return update("DrawMngDAO.UpdateEbomInfo", map);
    }
    
    /** EBOM 파트 삭제*/
    public int deleteEbomInfo(HashMap<String, Object> map) throws Exception {
    	return (Integer)delete("DrawMngDAO.deleteEbomInfo", map);
    }
    
    /** 도면 정보 삭제*/
	public int deleteDrawInfo(Map<String, Object> map) {
		return (Integer)delete("DrawMngDAO.deleteDrawInfo", map);
	}
	
    /** 도면 EBOM 정보 삭제*/
	public int deleteDrawEbomInfo(Map<String, Object> map) {
		return (Integer)delete("DrawMngDAO.deleteDrawEbomInfo", map);
	}
	public int deleteDrawEbomInfo2(Map<String, Object> map) {
		return (Integer)delete("DrawMngDAO.deleteDrawEbomInfo2", map);
	}
    public Object updateReturnEbomInfo(HashMap<String, Object> map) throws Exception {
    	return update("DrawMngDAO.updateReturnEbomInfo", map);
    }
	
	
    public List<Map<String, Object>> selectPrevDno(HashMap<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectPrevDno", map);
    }
	
	/** 도면 파일 정보 삭제*/
	public void deleteDrawFilesInfo(HashMap<String, Object> map) {
		delete("DrawMngDAO.deleteDrawFileInfo", map);
	}
	
	/** 도면 관련 모듈 정보*/
    public List<Map<String, Object>> selectRelMoudleInfo(HashMap<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectRelMoudleInfo", map);
    }
    
	/** 도면 bom 정보*/
    public List<Map<String, Object>> selectRelEbomInfo(HashMap<String, Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectRelEbomInfo", map);
    }
	
    public List<Map<String, Object>> selectModuleFileInfo(Map<String, Object> map) {
		return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectModuleFileInfo", map);
	}
	
    /** 파트 임시테이블 정보 추출 */
    public List<Map<String, Object>> selectTempVerPrtList(Map<String, Object> map) {
		return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectTempVerPrtList", map);
	}
    public List<Map<String, Object>> selectMBomCheckList(Map<String, Object> map) {
		return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectMBomCheckList", map);
	}
    /** 추출한 파트 임시테이블 정보 등록 */
    public Object insertVerPrtMigInfo(Map<String, Object> map) throws Exception {
    	return insert("DrawMngDAO.insertVerPrtMigInfo", map);
    }
    public Object deleteMBomInfo(Map<String, Object> map) throws Exception {
    	return delete("DrawMngDAO.deleteMBomInfo", map);
    }
    
    /** 도면 임시테이블 정보 추출 */
    public List<Map<String, Object>> selectTempModList(Map<String, Object> map) {
		return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectTempModList", map);
	}
    public List<Map<String, Object>> selectEBomCheckList(Map<String, Object> map) {
		return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectEBomCheckList", map);
	}
    public Object deleteEBomInfo(Map<String, Object> map) throws Exception {
    	return delete("DrawMngDAO.deleteEBomInfo", map);
    }
    public Object updateEbomChgSeq(Map<String, Object> map) throws Exception {
    	return update("DrawMngDAO.updateEbomChgSeq", map);
    }
    
    /** 도면 임시파일테이블 정보 추출 */
    public List<Map<String, Object>> selectTempModfileList(Map<String, Object> map) {
		return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectTempModfileList", map);
	}
    
    /** 추출한 도면 임시테이블 정보 등록 */
    public Object insertModMigInfo(Map<String, Object> map) throws Exception {
    	return insert("DrawMngDAO.insertModMigInfo", map);
    }
    
    /** 추출한 도면파일 임시테이블 정보 등록 */
    public Object insertModfileMigInfo(Map<String, Object> map) throws Exception {
    	return insert("DrawMngDAO.insertModfileMigInfo", map);
    }
    
    /** 설변 임시테이블 정보 추출 */
    public List<Map<String, Object>> selectTempEcList(Map<String, Object> map) {
		return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectTempEcList", map);
	}
    
    public List<Map<String, Object>> selectTempEcrelInfoList(Map<String, Object> map) {
		return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectTempEcrelInfoList", map);
	}
    
    /** 추출한 설변 임시테이블 정보 등록 */
    public Object insertEcMigInfo(Map<String, Object> map) throws Exception {
    	return insert("DrawMngDAO.insertEcMigInfo", map);
    }
    public Object insertEcContMigInfo(Map<String, Object> map) throws Exception {
    	return insert("DrawMngDAO.insertEcContMigInfo", map);
    }
    public Object insertEcRelInfoMigInfo(Map<String, Object> map) throws Exception {
    	return insert("DrawMngDAO.insertEcRelInfoMigInfo", map);
    }
    
	public int updateModuleFileInfo(Map<String, Object> map) throws Exception {
    	return (Integer)update("DrawMngDAO.updateModuleFileInfo", map);
    }
	
    public void insertDrawFileInfo(Map<String, Object> map) throws Exception {
    	insert("DrawMngDAO.insertDrawFileInfo", map);
    }
    
    public int updateVerprtRelMod(HashMap<String, Object> map) throws Exception {
    	return (Integer)update("DrawMngDAO.updateVerprtRelMod", map);
    }
    
    public int deleteVerprtRelMod(HashMap<String, Object> map) throws Exception {
    	return (Integer)delete("DrawMngDAO.deleteVerprtRelMod", map);
    }
    
    public List<Map<String, Object>> retrieveEbomDrawVerChkList(HashMap<String, Object> map) throws Exception{
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.retrieveEbomDrawVerChkList", map);
    }
    
    public List<Map<String, Object>> selectEbomInfo(HashMap<String, Object> map) throws Exception{
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectEbomInfo", map);
    }
    
    public Object updateEbomSeq(Map<String, Object> map) throws Exception {
    	return update("DrawMngDAO.updateEbomSeq", map);
    }
    
    public List<Map<String, Object>> selectSumEbomTreeList(HashMap<String,Object> map) throws Exception {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectSumEbomTreeList", map);
    }
    
    public void updateCheckInCADBomUpdate(Map<String, Object> map) throws Exception {
    	update("DrawMngDAO.updateCheckInCADBomUpdate", map);
    }
    
    
    /**
     * 도면리스트
     */
    public List<Map<String, Object>> retrieveDrawList(HashMap<String, Object> map) throws Exception{
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.retrieveDrawList", map);
    }
    
    public List<Map<String, Object>> selectBomRootList(Map<String, Object> map) {
    	return (List<Map<String, Object>>)getSqlMapClientTemplate().queryForList("DrawMngDAO.selectBomRootList", map);
	}
    
    public List<Map<String, Object>> selectDistReceiveTeamList(Map<String, Object> commandMap) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectDistReceiveTeamList", commandMap);
	}
    
	public List<Map<String, Object>> retrieveDocInfo(HashMap<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.retrieveDocInfo", map);
	}

	public void insertVerdochistory(Map<String, Object> map) {
		insert("DrawMngDAO.insertVerdochistory", map);
	}

	public List<Map<String, Object>> selectDistDocList(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectDistDocList", map);
	}
	
    public void updateBomTreeRootoid(HashMap<String, Object> map) throws Exception {
    	update("DrawMngDAO.updateBomTreeRootoid", map);
    }
	
    public void updateDrawCheckUnlock(Map<String, Object> map) throws Exception {
    	update("DrawMngDAO.updateDrawCheckUnlock", map);
    }
    
    public void updateDrawCheckLock(Map<String, Object> map) throws Exception {
    	update("DrawMngDAO.updateDrawCheckLock", map);
    }
    
    public void updateDrawFileCheckUnlock(Map<String, Object> map) throws Exception {
    	update("DrawMngDAO.updateDrawFileCheckUnlock", map);
    }
    
	public void insertModFileHistory(Map<String, Object> map) {
		insert("DrawMngDAO.insertModFileHistory", map);
	}
	
    public void updateModFileCheckIn(Map<String, Object> map) throws Exception {
    	update("DrawMngDAO.updateModFileCheckIn", map);
    }

    public void updateDrawStatus(Map<String, Object> map) throws Exception {
    	update("DrawMngDAO.updateDrawStatus", map);
    }
    
    public void insertEBOMTree(List<Map<String, Object>> map) {
		insert("DrawMngDAO.insertEBOMTree", map);
	}
    
    public void insertThumbnail(List<Map<String, Object>> map) {
		insert("DrawMngDAO.insertThumbnail", map);
	}
    
    public void insertAddEBOMTree(List<Map<String, Object>> map) {
		insert("DrawMngDAO.insertAddEBOMTree", map);
	}
 
    /** CAD I/G 관련 쿼리 */
    public List<Map<String, Object>> selectSearchDrawThumb(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectSearchDrawThumb", map);
	}
    
    public List<Map<String, Object>> selectSearchDraw(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectSearchDraw", map);
	}
    
    public List<Map<String, Object>> selectSearchDraw2(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectSearchDraw2", map);
	}
    
    public List<Map<String, Object>> selectCar(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectCar", map);
	}
    
    public List<Map<String, Object>> selectCancledraw(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectCancledraw", map);
	}
    
    public List<Map<String, Object>> selectMainSearchParent(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectMainSearchParent", map);
	}
    
    public List<Map<String, Object>> selectMainSearchChild(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectMainSearchChild", map);
	}
    
    public List<Map<String, Object>> selectEBOMTreeChild(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectEBOMTreeChild", map);
	}
    
    public List<Map<String, Object>> selectMainSearchModfiles(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectMainSearchModfiles", map);
	}
    
    public List<Map<String, Object>> selectMainSearchModfilehistory(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectMainSearchModfilehistory", map);
	}
    
    public void updateModCheckOut(Map<String, Object> map) throws Exception {
    	update("DrawMngDAO.updateModCheckOut", map);
    }
    
    public void updateModCheckOut2(Map<String, Object> map) throws Exception {
    	update("DrawMngDAO.updateModCheckOut2", map);
    }
    
    public void updateModfilesCheckOut(Map<String, Object> map) throws Exception {
    	update("DrawMngDAO.updateModfilesCheckOut", map);
    }
    
//    public void updateModCancleCheckOut(Map<String, Object> map) throws Exception {
//    	update("DrawMngDAO.updateModCancleCheckOut", map);
//    }
//    
//    public void updateModfilesCancleCheckOut(Map<String, Object> map) throws Exception {
//    	update("DrawMngDAO.updateModfilesCancleCheckOut", map);
//    }
    
    public void insertModfilehistory2(Map<String, Object> map) {
		insert("DrawMngDAO.insertModfilehistory2", map);
	}
    
    public void insertNoAddNewEbom(List<Map<String, Object>> map) {
		insert("DrawMngDAO.insertNoAddNewEbom", map);
	}
    
    public void updateCancleDraw(Map<String, Object> map) throws Exception {
    	update("DrawMngDAO.updateCancleDraw", map);
    }
    
    public List<Map<String, Object>> selectHumCheck(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectHumCheck", map);
	}
    
    public List<Map<String, Object>> selectModCheckIn(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectModCheckIn", map);
	}
    
    public String selectCheckInRootOid(String path) throws Exception {
    	return (String)select("DrawMngDAO.selectCheckInRootOid", path);
    }
    
	public List<Map<String, Object>> selectMainSearchModfiles2(List<String> arr) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectMainSearchModfiles2", arr);
	}
    
    public List<Map<String, Object>> selectEbomCheckIn(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectEbomCheckIn", map);
	}
    
    public List<Map<String, Object>> selectModfilesCheckIn(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectModfilesCheckIn", map);
	}
    
    public void updateNoAddfileCheckIn(Map<String,Object> map) throws Exception {
    	update("DrawMngDAO.updateNoAddfileCheckIn", map);
    }
    
    public void updateNoAddfileCheckIn2(Map<String, Object> map) throws Exception {
    	update("DrawMngDAO.updateNoAddfileCheckIn2", map);
    }
    
    public List<Map<String, Object>> selectAutoExistInfo(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectAutoExistInfo", map);
	}
    
    public List<Map<String, Object>> selectRegCatchError(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectRegCatchError", map);
	}
    
    public List<Map<String, Object>> selectRegCatchError2(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectRegCatchError2", map);
	}
    
    public List<Map<String, Object>> selectComtecopseq(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectComtecopseq", map);
	}
    
    public void insertAddDataMod(List<Map<String,Object>> map) throws Exception {
    	insert("DrawMngDAO.insertAddDataMod", map);
    }
    
    public void updateComtecopseq(Map<String, Object> map) throws Exception {
    	update("DrawMngDAO.updateComtecopseq", map);
    }
    
    public void insertDrawrel(List<Map<String,Object>> map) throws Exception {
    	insert("DrawMngDAO.insertDrawrel", map);
    }
    
    public void insertAddDataModfiles(List<Map<String,Object>> map) throws Exception {
    	insert("DrawMngDAO.insertAddDataModfiles", map);
    }
    
    public void insertGetEbomData(List<Map<String,Object>> map) throws Exception {
    	insert("DrawMngDAO.insertGetEbomData", map);
    }
    
    public void insertModfilesThumbNail(List<Map<String,Object>> map) throws Exception {
    	insert("DrawMngDAO.insertModfilesThumbNail", map);
    }
    
    public void updateAddDataModfiles(Map<String,Object> map) throws Exception {
    	update("DrawMngDAO.updateAddDataModfiles", map);
    }
    
    public List<Map<String, Object>> selectChkrootoid(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectChkrootoid", map);
	}
    
    public List<Map<String, Object>> selectChkOutEbomfiles(List<String> arr) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectChkOutEbomfiles", arr);
	}
    
    public List<Map<String, Object>> selectEbomFileList(List<String> arr) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectEbomFileList", arr);
	}
    
    
    public void deleteEbomList(Map<String,Object> map) {
		delete ("DrawMngDAO.deleteEbomList", map);
	}
//    public void deleteEbomList(List<String> arr) {
//		delete ("DrawMngDAO.deleteEbomList", arr);
//	}
    
    public List<Map<String, Object>> selectCCN(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectCCN", map);
	}
    
    //CAD IG에서 이미지 정보 조회
    public List<Map<String, Object>> selectCadigDrawInfo(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectCadigDrawInfo", map);
	}
    
    public void checkoutRevisionMod(Map<String, Object> map) throws Exception {
    	update("DrawMngDAO.checkoutRevisionMod", map);
    }
    
    public void updateEbomLastmodoid(Map<String, Object> map) throws Exception {
    	update("DrawMngDAO.updateEbomLastmodoid", map);
    }
    
    public void updateModStaoid(Map<String, Object> map) throws Exception {
    	update("DrawMngDAO.updateModStaoid", map);
    }
    
    public List<Map<String, Object>> selectgetModFiles(String oid) throws Exception {
    	return (List<Map<String, Object>>) list("DrawMngDAO.selectgetModFiles", oid);
    }
    
    public void checkoutRevisionModFiles(Map<String, Object> map) throws Exception {
    	update("DrawMngDAO.checkoutRevisionModFiles", map);
    }
    
    public List<Map<String, Object>> selectgetOriEbom(String oid) throws Exception {
    	return (List<Map<String, Object>>) list("DrawMngDAO.selectgetOriEbom", oid);
    }
    
    public List<Map<String, Object>> selectgetOriEbom2(String oid) throws Exception {
    	return (List<Map<String, Object>>) list("DrawMngDAO.selectgetOriEbom2", oid);
    }
    
    public String selectMaxVerDno(String dno) throws Exception {
    	return (String)select("DrawMngDAO.selectMaxVerDno", dno);
    }
    
    public String selectMaxVerPno(String pno) throws Exception {
    	return (String)select("DrawMngDAO.selectMaxVerPno", pno);
    }
    
//    public void checkoutRevisionEBOM(Map<String, Object> map) throws Exception {
//    	insert("DrawMngDAO.checkoutRevisionEBOM", map);
//    }
    
    public void checkoutRevisionEBOM(List<Map<String,Object>> map) throws Exception {
    	insert("DrawMngDAO.checkoutRevisionEBOM", map);
    }
    
    public List<Map<String, Object>> selectdrawrel(String oid) throws Exception {
    	return (List<Map<String, Object>>) list("DrawMngDAO.selectdrawrel", oid);
    }
    
    public List<Map<String, Object>> selectMaxPrtVersion(String oid) throws Exception {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectMaxPrtVersion", oid);
	}
    
    public List<Map<String, Object>> selectMaxPrtVersion2(String pno) throws Exception {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectMaxPrtVersion2", pno);
	}
    
    public void checkoutRevisionVerprt(Map<String, Object> map) throws Exception {
    	update("DrawMngDAO.checkoutRevisionVerprt", map);
    }
    
    public void updateVerprtStaoid(Map<String, Object> map) throws Exception {
    	update("DrawMngDAO.updateVerprtStaoid", map);
    }
    
    public void checkoutRevisionDrawRel(Map<String, Object> map) throws Exception {
    	insert("DrawMngDAO.checkoutRevisionDrawRel", map);
    }
    
    public void insertOnlyOneMod(Map<String, Object> map) throws Exception {
    	insert("DrawMngDAO.insertOnlyOneMod", map);
    }
    
    public void insertOnlyOneModFile(List<Map<String,Object>> map) throws Exception {
    	insert("DrawMngDAO.insertOnlyOneModFile", map);
    }
    
    public void updateCallBomMain(Map<String, Object> map) throws Exception {
    	update("DrawMngDAO.updateCallBomMain", map);
    }
    
    public List<Map<String, Object>> selectModfilesList(List<String> arr) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectModfilesList", arr);
	}
    
    public List<Map<String, Object>> selectModMaxVersion(List<String> arr) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectModMaxVersion", arr);
	}
    
    public List<Map<String, Object>> selectModDrawrel(List<String> arr) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectModDrawrel", arr);
	}
    
    public List<Map<String, Object>> selectEbomModOid(List<String> arr) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectEbomModOid", arr);
	}
    
    public String checkMaxVersion(Map<String, Object> map) throws Exception {
		return (String) select("DrawMngDAO.checkMaxVersion", map);
	}
    
    public void updateModCancleCheckout(List<String> arr) {
		update("DrawMngDAO.updateModCancleCheckout", arr);
	}
    
    public void updateModFilesCancleCheckout(List<String> arr) {
		update("DrawMngDAO.updateModFilesCancleCheckout", arr);
	}
    
    public void checkoutRevisionMBOM(Map<String, Object> map) throws Exception {
    	insert("DrawMngDAO.checkoutRevisionMBOM", map);
    }
    
    public List<Map<String, Object>> selectPartInfo(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectPartInfo", map);
	}
    
    public List<Map<String, Object>> selectComtecopseq2(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectComtecopseq2", map);
	}
    
    public void insertModfilesHistory(String path) throws Exception {
    	insert("DrawMngDAO.insertModfilesHistory", path);
    }
    
    public List<Map<String, Object>> selectCheckInModfiles(String path) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectCheckInModfiles", path);
	}
    
    public void updateModfilesCheckIn(Map<String, Object> map) throws Exception {
		update("DrawMngDAO.updateModfilesCheckIn", map);
	}
    
    public void updateModCheckIn(Map<String, Object> map) throws Exception {
    	update("DrawMngDAO.updateModCheckIn", map);
	}
    
    public List<Map<String, Object>> autoFillModInfo(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.autoFillModInfo", map);
	}
    
    public List<Map<String, Object>> selectRegCatchError3(List<String> arr) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectRegCatchError3", arr);
	}
    
    public List<Map<String, Object>> selectCancleStaoid(List<String> arr) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectCancleStaoid", arr);
	}
    
    public void updateCancleStaoid(List<String> arr) throws Exception {
		update("DrawMngDAO.updateCancleStaoid", arr);
	}
    
    public List<Map<String, Object>> selectOriEbomFileOid(List<String> arr) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectOriEbomFileOid", arr);
	}
    
    public void insertNewRegistEbom(List<Map<String,Object>> map) throws Exception {
    	insert("DrawMngDAO.insertNewRegistEbom", map);
    }
    
    public void insertModfiles2(Map<String, Object> map) throws Exception {
    	insert("DrawMngDAO.insertModfiles2", map);
    }
    
    public String selectMaxIndexNo(String modoid) throws Exception {
		return (String) select("DrawMngDAO.selectMaxIndexNo", modoid);
	}
    
    public List<Map<String, Object>> selectPnoMaxStaoid(List<String> arr) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectPnoMaxStaoid", arr);
	}
    
    public List<Map<String, Object>> selectMainParentLastoid(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectMainParentLastoid", map);
	}
    
    public List<Map<String, Object>> selectLatestEbomChild(Map<String, Object> map) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectLatestEbomChild", map);
	}
    
    public List<Map<String, Object>> selectMainSearchModfilehistory2(List<String> arr) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectMainSearchModfilehistory2", arr);
	}
    
    public List<Map<String, Object>> selectLatestModInfo(List<String> arr) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectLatestModInfo", arr);
	}
    
    public List<Map<String, Object>> selectCurMbom(List<String> arr) {
		return (List<Map<String, Object>>) list("DrawMngDAO.selectCurMbom", arr);
	}
    
    public void revisionMbom(List<Map<String,Object>> map) throws Exception {
    	insert("DrawMngDAO.revisionMbom", map);
    }
    
    public void deleteMbomList(Map<String,Object> map) {
		delete ("DrawMngDAO.deleteMbomList", map);
	}
    
    public String selectModInfo(Map<String, Object> map) throws Exception {
		return (String) select("DrawMngDAO.selectModInfo", map);
	}
    
    public String selectMaxModoid(Map<String, Object> map) throws Exception {
		return (String) select("DrawMngDAO.selectMaxModoid", map);
	}
    
}