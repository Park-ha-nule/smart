package egovframework.admin.recsroom.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import egovframework.admin.common.vo.UserInfoVo;
import egovframework.admin.quickMenu.web.QuickMenuMgtController;
import egovframework.admin.recsroom.service.RecsroomMgtService;
import egovframework.common.service.CodeCacheService;
import egovframework.framework.common.constant.Const;
import egovframework.framework.common.constant.Globals;
import egovframework.framework.common.object.DataMap;
import egovframework.framework.common.page.util.pageNavigationUtil;
import egovframework.framework.common.util.EgovMessageSource;
import egovframework.framework.common.util.EgovPropertiesUtil;
import egovframework.framework.common.util.MessageUtil;
import egovframework.framework.common.util.RequestUtil;
import egovframework.framework.common.util.SessionUtil;
import egovframework.framework.common.util.SysUtil;
import egovframework.framework.common.util.TransReturnUtil;
import egovframework.framework.common.util.file.NtsysFileMngUtil;
import egovframework.framework.common.util.file.service.NtsysFileMngService;
import egovframework.framework.common.util.file.vo.NtsysFileVO;


@Controller
public class RecsroomMgtController {
	private static Log log = LogFactory.getLog(RecsroomMgtController.class);

	@Resource(name="recsroomMgtService")
	private RecsroomMgtService recsroomMgtService;

	@Resource(name="NtsysFileMngUtil")
    private NtsysFileMngUtil ntsysFileMngUtil;

	@Resource(name="NtsysFileMngService")
    private NtsysFileMngService ntsysFileMngService;

	@Resource(name="egovMessageSource")
    private EgovMessageSource egovMessageSource;

	/**
	 *
	 * <PRE>
	 * 1. MethodName : selectPageListRecsroomMgt
	 * 2. ClassName  : recsroomMgtController
	 * 3. Comment   : ????????? ?????? ??????
	 * 4. ?????????    : Kim sung min
	 * 5. ?????????    : 2021. 5. 3. ?????? 14:47:09
	 * </PRE>
	 *   @return String
	 *   @param sys
	 *   @param request
	 *   @param response
	 *   @param model
	 *   @return
	 *   @throws Exception
	 */

	@RequestMapping(value = { "/admin/recsroom/selectPageListRecsroomMgt.do", "/admin/recsroom/{sys}/selectPageListRecsroomMgt.do" })
    public String selectPageListRecsroomMgt(@PathVariable(name = "sys", required = false) String sys, HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		log.debug("####" + this.getClass().getName() + " START ####");

		DataMap param = RequestUtil.getDataMap(request);

    	/* ### Pasing ?????? ### */
    	int totRecsroom = recsroomMgtService.selectTotRecsroomMgt(param);

    	param.put("totalCount", totRecsroom);
    	param = pageNavigationUtil.createNavigationInfo(model, param);
        List resultList = recsroomMgtService.selectPageListRecsroomMgt(param);
        /* ### Pasing ??? ### */

        model.addAttribute("totalCount", totRecsroom);
        model.addAttribute("resultList", resultList);
        model.addAttribute("param", param);

        return "admin/recsroom/selectPageListRecsroomMgt";
	}

	/**
	 * <PRE>
	 * 1. MethodName : insertFormRecsroomMgt
	 * 2. ClassName  : recsroomMgtController
	 * 3. Comment   : ????????? ?????????
	 * 4. ?????????    : Kim sung min
	 * 5. ?????????    : 2021. 5. 03. ?????? 15:36:47
	 * </PRE>
	 *   @return String
	 *   @param request
	 *   @param response
	 *   @param model
	 *   @return
	 *   @throws Exception
	 */
	@RequestMapping(value= { "/admin/recsroom/insertFormRecsroomMgt.do", "/admin/bbs/{sys}/insertFormRecsroomMgt.do" })
	public String insertFormRecsroomMgt(@PathVariable(name = "sys", required = false) String sys, HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {

		DataMap param = RequestUtil.getDataMap(request);

		if (sys != null) {
			param.put("sys_mapping_code", sys);
		}

		model.addAttribute("param", param);

		return "admin/recsroom/insertFormRecsroomMgt";
	}

	/**
	 * <PRE>
	 * 1. MethodName : insertRecsroomMgt
	 * 2. ClassName  : recsroomMgtController
	 * 3. Comment  : ????????? ??????
	 * 4. ?????????   : ?????????
	 * 5. ?????????   : 2021. 5. 03
	 * </PRE>
	 *   @return String
	 *   @param request
	 *   @param response
	 *   @param model
	 *   @return
	 *   @throws Exception
	 */
	@RequestMapping(value = { "/admin/recsroom/insertRecsroomMgt.do", "/admin/recsroom/{sys}/insertRecsroomMgt.do" })
	public @ResponseBody DataMap insertRecsroomMgt(@PathVariable(name = "sys", required = false) String sys, HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		System.out.println(" ????????? ??????");
		DataMap param = RequestUtil.getDataMap(request);
		DataMap resultStats = new DataMap();

		/* ### ????????? ?????? ?????? ?????? ###*/
    	List<MultipartFile> fileList = new ArrayList<MultipartFile>();
    	String[] imgPathList = request.getParameterValues("imgPath");
    	param.put("imgPathList", imgPathList);
    	String dftFilePath = EgovPropertiesUtil.getProperty("Globals.fileImgTempPath");
    	if(!(imgPathList== null))
    	fileList = fileListSet(imgPathList, dftFilePath);
		 /*### ????????? ?????? ?????? ??? ###*/

		//???????????? returnUrl ??????
    	String returnUrl = "";
    	if (sys != null) {
    		returnUrl = "/admin/recsroom/"+ sys +"/selectRecsroomMgt.do";
        } else {
        	returnUrl = "/admin/recsroom/selectRecsroomMgt.do";
        }

    	//????????? ?????? ????????? ?????? ??????//
    	List attachFileList = ntsysFileMngUtil.getFiles((MultipartHttpServletRequest)request);

    	DataMap resultJSON = new DataMap();
    	MessageUtil.setMessage(request, egovMessageSource.getMessage("succ.data.insert"));
    	resultStats.put("resultCode", "ok");

    	// ?????? ????????? ??????
    	String msg = ntsysFileMngUtil.checkFileExt(attachFileList);
    	if(msg != ""){
    	MessageUtil.setMessage(request, egovMessageSource.getMessage("error.file.not.access", new String[] { msg }));
    	resultStats.put("resultCode", "error");
    	returnUrl = "/admin/recsroom/insertFormRecsroomMgt.do?";
    	}

    	// ?????? ?????? ??????(???????????? ??????????????? ?????? ?????? ???????????? ??????)
    	if (!ntsysFileMngUtil.checkFileSize(attachFileList)) {
    		MessageUtil.setMessage(request, egovMessageSource.getMessage("error.file.size.over", new String[] { ntsysFileMngUtil.getFileSize(EgovPropertiesUtil.getProperty("Globals.fileMaxSize")) }));
    		resultStats.put("resultCode", "error");
    		returnUrl = "/admin/recsroom/insertFormRecsroomMgt.do?";
    	}

    	//????????? ??????  ???//

    	UserInfoVo userInfoVo = SessionUtil.getSessionUserInfoVo(request);
    	param.put("ss_user_no", userInfoVo.getUserNo());
    	recsroomMgtService.insertRecsroomMgt(param, fileList, attachFileList);
    	model.addAttribute("param", param);

    	returnUrl += "?recsroom_seq="+ param.getString("recsroom_seq");

    	resultStats.put("redirectUrl",returnUrl);
		resultStats.put("resultMsg", "");
    	resultJSON.put("resultStats", resultStats);// ????????? : ????????? ?????? ??????
    	response.setContentType("text/html; charset=utf-8");

        return resultJSON;

	}

	/**
	 * <PRE>
	 * 1. MethodName : fileListSet
	 * 2. ClassName  : recsroomMgtController
	 * 3. Comment   : ???????????? ????????? ????????? List??? ??????
	 * 4. ?????????    : ?????????
	 * 5. ?????????    : 2018. 7. 12. ?????? 4:35:21
	 * </PRE>
	 *   @return List
	 *   @param imgPathList
	 *   @param dftFilePath
	 *   @return
	 */
	public List<MultipartFile> fileListSet(String[] imgPathList, String dftFilePath) {
		// TODO Auto-generated method stub
    	List<MultipartFile> fileList = new ArrayList<MultipartFile>();

		if(imgPathList.length > 0){

	    	for(String str : imgPathList){
	    		File file = new File(dftFilePath + str);
			    MultipartFile multipartFile = fileToMultipartFile(file);
			    fileList.add(multipartFile);
	    	}
		}
		return fileList;
	}

	/**
	 * <PRE>
	 * 1. MethodName : fileToMultipartFile
	 * 2. ClassName  : CntsMgtController
	 * 3. Comment   : file ????????? MultipartFile ????????? ??????
	 * 4. ?????????    : ?????????
	 * 5. ?????????    : 2018. 7. 12. ?????? 4:35:21
	 * </PRE>
	 *   @return MultipartFile
	 *   @param file
	 *   @return
	 */
	public MultipartFile fileToMultipartFile(File file) {
	    FileItem fileItem = null;
	    try {
	        fileItem = new DiskFileItem(null, Files.probeContentType(file.toPath()), false, file.getName(), (int) file.length(), file.getParentFile());
	    } catch (IOException e1) {
	        e1.printStackTrace();
	    }

	    InputStream inputStream = null;
	    try {
	        inputStream = new FileInputStream(file);
	        OutputStream outputStream = fileItem.getOutputStream();
	        IOUtils.copy(inputStream, outputStream);
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    MultipartFile multipartFile = new CommonsMultipartFile(fileItem);

	    return multipartFile;
	}

	/**
	 * <PRE>
	 * 1. MethodName : selectRecsroomMgt
	 * 2. ClassName  : RecsroomMgtController
	 * 3. Comment  : ????????? ?????? ??????
	 * 4. ?????????   : ?????????
	 * 5. ?????????   : 2021. 5. 04
	 * </PRE>
	 *   @return String
	 *   @param request
	 *   @param response
	 *   @param model
	 *   @return
	 *   @throws Exception
	 */
	@RequestMapping(value = { "/admin/recsroom/selectRecsroomMgt.do", "/admin/recsroom/{sys}/selectRecsroomMgt.do" })
	public String selectRecsroomMgt(@PathVariable(name = "sys", required = false) String sys, HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {

		DataMap param = RequestUtil.getDataMap(request);

		if (sys != null) {
			param.put("sys_mapping_code", sys);
		}

		DataMap resultMap = recsroomMgtService.selectRecsroomMgt(request, response, param);
		resultMap.put("CN", resultMap.htmlTagFilterRestore(resultMap.getString("CN")));

		// #### FILE LIST ?????? Start ####
		NtsysFileVO fvo = new NtsysFileVO();
		fvo.setDocId(resultMap.getString("DOC_ID"));

		List<NtsysFileVO> fileList = ntsysFileMngService.selectFileInfs(fvo);
		// #### FILE LIST ?????? End ####

		model.addAttribute("fileList", fileList);
		model.addAttribute("resultMap", resultMap);
		model.addAttribute("param", param);

		return "admin/recsroom/selectRecsroomMgt";
	}
	/**
	 * <PRE>
	 * 1. MethodName : deleteRecsroomMgt
	 * 2. ClassName  : RecsroomMgtController
	 * 3. Comment  : ????????? ??????
	 * 4. ?????????   : ?????????
	 * 5. ?????????   : 2021. 5. 06
	 * </PRE>
	 *   @return void
	 *   @param request
	 *   @param response
	 *   @param model
	 *   @return
	 *   @throws Exception
	 */
	@RequestMapping(value = { "/admin/recsroom/deleteRecsroomMgt.do", "/admin/recsroom/{sys}/deleteRecsroomMgt.do" })
	public String deleteRecsroomMgt(@PathVariable(name = "sys", required = false) String sys, HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {

		// ????????? ??? returnUrl ??????
		String returnUrl = "";
		if (sys != null) {
			returnUrl = "/admin/recsroom/" + sys;
		} else {
			returnUrl = "/admin/recsroom";
		}

		UserInfoVo userInfoVo = SessionUtil.getSessionUserInfoVo(request);

		DataMap param = RequestUtil.getDataMap(request);

		param.put("ss_user_no", userInfoVo.getUserNo());
		model.addAttribute("param", param);

		recsroomMgtService.deleteRecsroomMgt(param);

		MessageUtil.setMessage(request, egovMessageSource.getMessage("succ.data.delete"));

		return TransReturnUtil.returnPage(Globals.METHOD_GET, returnUrl + "/selectPageListRecsroomMgt.do", param, model);

	}

	/**
	 * <PRE>
	 * 1. MethodName : updateFormRecsroomMgt
	 * 2. ClassName  : RecsroomMgtController
	 * 3. Comment  : ????????? ?????? ???
	 * 4. ?????????   : ?????????
	 * 5. ?????????   : 2021. 5. 07
	 * </PRE>
	 *   @return String
	 *   @param request
	 *   @param response
	 *   @param model
	 *   @return
	 *   @throws Exception
	 */
	@RequestMapping(value = { "/admin/recsroom/updateFormRecsroomMgt.do", "/admin/recsroom/{sys}/updateFormRecsroomMgt.do" })
	public String updateFormRecsroomMgt(@PathVariable(name = "sys", required = false) String sys, HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {

		DataMap param = RequestUtil.getDataMap(request);

		//????????? ?????? ?????? ??????
		DataMap resultMap = recsroomMgtService.selectRecsroomMgt(request, response, param);

		// #### FILE LIST ?????? Start ####
 		NtsysFileVO fvo = new NtsysFileVO();
 		fvo.setDocId(resultMap.getString("DOC_ID"));

 		List<NtsysFileVO> fileList = ntsysFileMngService.selectFileInfs(fvo);
 		// #### FILE LIST ?????? End ####

		model.addAttribute("fileList", fileList);
		model.addAttribute("resultMap", resultMap);
		model.addAttribute("param", param);

		return "admin/recsroom/updateFormRecsroomMgt";
	}

	/**
	 * <PRE>
	 * 1. MethodName : updateRecsroomMgt
	 * 2. ClassName  : RecsroomMgtController
	 * 3. Comment  : ????????? ??????
	 * 4. ?????????   : ?????????
	 * 5. ?????????   : 2021. 5. 10
	 * </PRE>
	 *   @return String
	 *   @param request
	 *   @param response
	 *   @param model
	 *   @return
	 *   @throws Exception
	 */

	@RequestMapping(value = {"/admin/recsroom/updateRecsroomMgt.do", "/admin/recsroom/{sys}/updateRecsroomMgt.do"})
	public @ResponseBody DataMap updateCntsMgt(@PathVariable(name = "sys", required = false) String sys, HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {

		UserInfoVo userInfoVo = SessionUtil.getSessionUserInfoVo(request);
		DataMap param = RequestUtil.getDataMap(request);
		param.put("ss_user_no", userInfoVo.getUserNo());

		DataMap resultJSON = new DataMap();
		DataMap resultStats = new DataMap();

		// ???????????? returnUrl ??????
				String returnUrl = "";
				if (sys != null) {
					returnUrl = "/admin/recsroom/" + sys;
				} else {
					returnUrl = "/admin/recsroom";
				}

		// ?????? ????????? ?????? ??????//
		List<MultipartFile> fileList = new ArrayList<MultipartFile>();
		String[] imgPathList = request.getParameterValues("imgPath");
		param.put("imgPathList", imgPathList);
		String dftFilePath = EgovPropertiesUtil.getProperty("Globals.fileImgTempPath");
		if (!(imgPathList == null))
		fileList = fileListSet(imgPathList, dftFilePath);
		// ?????? ????????? ?????? ??????//

		// ????????? ?????? ????????? ?????? ??????//
		List attachFileList = ntsysFileMngUtil.getFiles((MultipartHttpServletRequest)request);
		resultStats.put("resultCode", "ok");

		// ?????? ????????? ??????
		String msg = ntsysFileMngUtil.checkFileExt(attachFileList);
		if(msg != ""){
		MessageUtil.setMessage(request, egovMessageSource.getMessage("error.file.not.access", new String[] { msg }));
		resultStats.put("resultCode", "error");
		}

		// ?????? ?????? ??????(???????????? ??????????????? ?????? ?????? ???????????? ??????)
		if (!ntsysFileMngUtil.checkFileSize(attachFileList)) {
		MessageUtil.setMessage(request, egovMessageSource.getMessage( "error.file.size.over", new String[] { ntsysFileMngUtil.getFileSize(EgovPropertiesUtil.getProperty("Globals.fileMaxSize")) }));
		resultStats.put("resultCode", "error");
		}
		//????????? ?????? ????????? ?????? ???//

		model.addAttribute("param", param);

		recsroomMgtService.updateRecsroomMgt(param,fileList,attachFileList);

		resultStats.put("resultMsg", "");
		resultJSON.put("resultStats", resultStats);// ????????? : ????????? ?????? ??????

		MessageUtil.setMessage(request, egovMessageSource.getMessage("succ.data.update"));

		DataMap resultParam = new DataMap();

		DataMap redirectParam = new DataMap();
		redirectParam.put("recsroom_seq", param.getString("recsroom_seq"));
		resultParam.put("returnParam", SysUtil.createUrlParam(redirectParam));
		resultParam.put("returnUrl", returnUrl + "/selectRecsroomMgt.do");
		resultJSON.put("resultParam", resultParam);
		response.setContentType("text/html; charset=utf-8");

		return resultJSON;
	}
}
