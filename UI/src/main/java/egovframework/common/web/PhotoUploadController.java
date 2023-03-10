package egovframework.common.web;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import egovframework.admin.user.web.UserMgtController;
import egovframework.common.vo.PhotoVo;
import egovframework.framework.common.dao.CommonMybatisDao;
import egovframework.framework.common.object.DataMap;
import egovframework.framework.common.util.EgovMessageSource;
import egovframework.framework.common.util.EgovPropertiesUtil;
import egovframework.framework.common.util.MessageUtil;
import egovframework.framework.common.util.RequestUtil;
import egovframework.framework.common.util.file.NtsysFileMngUtil;
import egovframework.framework.common.util.file.service.NtsysFileMngService;
import egovframework.framework.common.util.file.vo.NtsysFileVO;

@Controller
public class PhotoUploadController {

	/** commonDao */
	@Resource(name="commonMybatisDao")
	private CommonMybatisDao commonMybatisDao;

	@Resource(name="egovMessageSource")
	private EgovMessageSource egovMessageSource;

	@Resource(name="NtsysFileMngService")
	private NtsysFileMngService ntsysFileMngService;

	@Resource(name="NtsysFileMngUtil")
	private NtsysFileMngUtil ntsysFileMngUtil;

	private static Log log = LogFactory.getLog(UserMgtController.class);

	//?????????????????????
	@RequestMapping(value = "/photoUpload.do")
	public String photoUpload(HttpServletRequest request, PhotoVo vo){
		String callback = vo.getCallback();
		String callbackFunc = vo.getCallbackFunc();
		String fileResult = "";

		try {
			if(vo.getFiledata() != null && vo.getFiledata().getOriginalFilename() != null && !vo.getFiledata().getOriginalFilename().equals("")){
				//????????? ????????????
				String originalName = vo.getFiledata().getOriginalFilename();
				String ext = originalName.substring(originalName.lastIndexOf(".")+1);
				//?????? ????????????
				String defaultPath = request.getSession().getServletContext().getRealPath("/");
				//?????? ???????????? _ ????????????
				String path = defaultPath + "resource" + File.separator + "photo_upload" + File.separator;
				/*String path = EgovPropertiesUtil.getProperty("img.upload.path");*/
				File file = new File(path);
				//???????????? ???????????? ???????????? ???????????? ??????
				if(!file.exists()) {
					file.mkdirs();
				}
				//????????? ????????? ??? ?????????(??????????????? ?????? ??????????????? ????????? ???????????? ??????)
				String realname = UUID.randomUUID().toString() + "." + ext;
			///////////////// ????????? ???????????? /////////////////
				vo.getFiledata().transferTo(new File(path+realname));
				fileResult += "&bNewLine=true&sFileName="+originalName+"&sFileURL=/resource/photo_upload/"+realname;
			} else {
				fileResult += "&errstr=error";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:" + callback + "?callback_func=" + callbackFunc + fileResult;
	}


	//?????????????????????
	/**
	* <PRE>
	* 1. MethodName : multiplePhotoUpload
	* 2. ClassName  : PhotoUploadController
	* 3. Comment   :
	* 4. ?????????	: ?????????
	* 5. ?????????	: 2018. 7. 4. ?????? 2:04:59
	* </PRE>
	*   @return void
	*   @param request
	*   @param response
	*/
	@RequestMapping(value = "/multiplePhotoUpload.do")
	public void multiplePhotoUpload(HttpServletRequest request, MultipartHttpServletRequest mtfRequest, HttpServletResponse response){
		try {
			DataMap param = RequestUtil.getDataMap(request);

			List fileList = ntsysFileMngUtil.getFiles((MultipartHttpServletRequest)request);
			NtsysFileVO reNtsysFile = null;

			String sFileInfo = ""; //????????? ??? ?????? ??????
			response.setContentType("text/html; charset=utf-8");
			PrintWriter print = response.getWriter();

			// ?????? ?????? ??????(???????????? ??????????????? ?????? ?????? ???????????? ??????)
			if (!ntsysFileMngUtil.checkFileSize(fileList, "Globals.ImgfileMaxSize")) {
				MessageUtil.setMessage(request, egovMessageSource.getMessage(
						"error.file.size.over", new String[] { ntsysFileMngUtil.getFileSize(EgovPropertiesUtil.getProperty("Globals.ImgfileMaxSize")) }));
				print.print("ErrorSizeFile");
				print.flush();
				print.close();

			}else if(ntsysFileMngUtil.checkFileExt(fileList) != ""){
				// ?????? ????????? ??????
					MessageUtil.setMessage(request, egovMessageSource.getMessage("error.file.not.access",
							new String[] { "" }));
					print.print("ErrorExtFile");
					print.flush();
					print.close();
			}else{
				if(!fileList.isEmpty()){
					for(int i=0; i < fileList.size(); i++){
						MultipartFile mfile = (MultipartFile)fileList.get(i);
						if(!mfile.isEmpty()){
							/**
							* parseFileInf
							* 1:????????????
							* 2:???????????????
							* 3:???????????????
							* 4:???????????????
							* 5:Web Root Yn
							*/
							// ????????? ????????? ??????????????? ??????
							reNtsysFile	= ntsysFileMngUtil.parseFileInf(mfile, "", "temp/", param.getString("ss_user_no"), "Y");

							//????????????
							// img ????????? title ????????? ????????????????????? ?????????????????? ??????
							sFileInfo += "&sFileName="+ reNtsysFile.getFileNm();
							sFileInfo += "&sFileURL="+ reNtsysFile.getFileRltvPath() + reNtsysFile.getFileId() + "." + reNtsysFile.getFileExtNm();
						}
					}
				}

				print.print(sFileInfo);
				print.flush();
				print.close();
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 *
	 * <PRE>
	 * 1. MethodName : multipartHttpPhotoUpload
	 * 2. ClassName  : PhotoUploadController
	 * 3. Comment   : ckeditor ????????? ????????? httpServlertRequest??? MultipartHttpServletRequest??? ??????
	 * 4. ?????????    : :LWJ
	 * 5. ?????????    : 2020. 6. 8. ?????? 1:34:40
	 * </PRE>
	 *   @return void
	 *   @param request
	 *   @param mtfRequest
	 *   @param response
	 */
	@RequestMapping(value = "/multipartHttpPhotoUpload.do")
	public void multipartHttpPhotoUpload(MultipartHttpServletRequest request, MultipartHttpServletRequest mtfRequest, HttpServletResponse response){
		try {
			DataMap param = RequestUtil.getDataMap(request);

			//List fileList = ntsysFileMngUtil.getFiles((MultipartHttpServletRequest)request);
			List fileList = ntsysFileMngUtil.getFiles(request);
			NtsysFileVO reNtsysFile = null;

			String sFileInfo = ""; //????????? ??? ?????? ??????
			response.setContentType("text/html; charset=utf-8");
			PrintWriter print = response.getWriter();

			// ?????? ?????? ??????(???????????? ??????????????? ?????? ?????? ???????????? ??????)
			if (!ntsysFileMngUtil.checkFileSize(fileList, "Globals.ImgfileMaxSize")) {
				MessageUtil.setMessage(request, egovMessageSource.getMessage("error.file.size.over", new String[] { ntsysFileMngUtil.getFileSize(EgovPropertiesUtil.getProperty("Globals.ImgfileMaxSize")) }));
				print.print("ErrorSizeFile");
				print.flush();
				print.close();
			} else if (ntsysFileMngUtil.checkFileExt(fileList) != "") {
				// ?????? ????????? ??????
				MessageUtil.setMessage(request, egovMessageSource.getMessage("error.file.not.access",new String[] { "" }));
				print.print("ErrorExtFile");
				print.flush();
				print.close();
			} else {
				if (!fileList.isEmpty()) {
					for (int i = 0; i < fileList.size(); i++) {
						MultipartFile mfile = (MultipartFile) fileList.get(i);
						if (!mfile.isEmpty()) {
							/**
							* parseFileInf
							* 1:????????????
							* 2:???????????????
							* 3:???????????????
							* 4:???????????????
							* 5:Web Root Yn
							*/
							// ????????? ????????? ??????????????? ??????
							reNtsysFile = ntsysFileMngUtil.parseFileInf(mfile, "", "temp/", param.getString("ss_user_no"), "Y");

							// ????????????
							// img ????????? title ????????? ????????????????????? ?????????????????? ??????
							sFileInfo += "&sFileName=" + reNtsysFile.getFileNm();
							sFileInfo += "&sFileURL=" + reNtsysFile.getFileRltvPath() + reNtsysFile.getFileId() + "." + reNtsysFile.getFileExtNm();
						}
					}
				}

				print.print(sFileInfo);
				print.flush();
				print.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

/*
	@RequestMapping(value = "/multiplePhotoUpload.do")
	public void multiplePhotoUpload(HttpServletRequest request, MultipartHttpServletRequest mtfRequest, HttpServletResponse response){
		try {
			DataMap param = RequestUtil.getDataMap(request);

			List fileList = ntsysFileMngUtil.getFiles((MultipartHttpServletRequest)request);
			NtsysFileVO _reNtsysFile = null;

			String sFileInfo = ""; //????????? ??? ?????? ??????

			if(!fileList.isEmpty()){
				for(int i=0; i < fileList.size(); i++){
					MultipartFile mfile = (MultipartFile)fileList.get(i);
					if(!mfile.isEmpty()){
						*//**
						* parseFileInf
						* 1:????????????
						* 2:???????????????
						* 3:???????????????
						* 4:???????????????
						* 5:Web Root Yn
						*//*
						// ????????? ????????? ??????????????? ??????
						_reNtsysFile	= ntsysFileMngUtil.parseFileInf(mfile, "", "temp/", param.getString("ss_user_no"), "Y");

						//????????????
						// img ????????? title ????????? ????????????????????? ?????????????????? ??????
						sFileInfo += "&sFileName="+ _reNtsysFile.getFile_nm();
						sFileInfo += "&sFileURL="+ _reNtsysFile.getFile_rltv_path() + _reNtsysFile.getFile_id() + "." + _reNtsysFile.getFile_ext_nm();
					}
				}
			}



			//???????????? ????????? - ?????? ???????????????
			String filename = param.getString("file-name");
			String filesize = param.getString("file-size");
			//?????? ?????????
			String filename_ext = filename.substring(filename.lastIndexOf(".")+1);
			//???????????????????????? ??????
			filename_ext = filename_ext.toLowerCase();

			//????????? ?????? ????????????
			String[] allow_file = {"jpg","png","bmp","gif","jpeg"};

			//???????????? ???????????? ???????????????
			int cnt = 0;
			for(int i=0; i<allow_file.length; i++) {
				if(filename_ext.equals(allow_file[i])){
					cnt++;
				}
			}

			//???????????? ??????
			if(cnt == 0) {
				PrintWriter print = response.getWriter();
				print.print("NOTALLOW_"+filename);
				print.flush();
				print.close();
			} else {
				List fileList = ntsysFileMngUtil.getFiles((MultipartHttpServletRequest)request);
				NtsysFileVO _reNtsysFile = null;

				if(!fileList.isEmpty()){
					for(int i=0; i < fileList.size(); i++){
						MultipartFile mfile = (MultipartFile)fileList.get(i);
						if(!mfile.isEmpty()){

							* parseFileInf
							* 1:????????????
							* 2:???????????????
							* 3:???????????????
							* 4:???????????????
							* 5:Web Root Yn

							// ????????? ????????? ??????????????? ????????????
							_reNtsysFile	= ntsysFileMngUtil.parseFileInf(mfile, "", "temp/", "user_id", "Y");


							// ????????? ?????????????????? ????????? ???????????? ????????? DB??? ?????????.
							commonMybatisDao.insert("common.file.insertAttchFile", _reNtsysFile);

						}
					}
				}

				///////////////// ????????? ???????????? /////////////////
			}
				// ?????? ??????

				PrintWriter print = response.getWriter();
				print.print(sFileInfo);
				print.flush();
				print.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/

	/**
	 *
	 * <PRE>
	 * 1. MethodName : fileUpload
	 * 2. ClassName  : PhotoUploadController
	 * 3. Comment   : ckeditor ????????? ?????????
	 * 4. ?????????    : :LWJ
	 * 5. ?????????    : 2020. 6. 4. ?????? 3:13:50
	 * </PRE>
	 *   @return void
	 *   @param request
	 *   @param response
	 *   @param multiFile
	 *   @param upload
	 *   @throws Exception
	 */
	@RequestMapping(value = "/photoServerUpload.do",method=RequestMethod.POST)
	public void fileUpload(MultipartHttpServletRequest request, HttpServletResponse response, MultipartHttpServletRequest multiFile, @RequestParam MultipartFile upload) throws Exception{
		  // ?????? ?????? ??????
        UUID uid = UUID.randomUUID();

        OutputStream out = null;
        PrintWriter printWriter = null;

        //?????????
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");

		try {
			// ?????? ?????? ????????????
			String fileName = upload.getOriginalFilename();
			byte[] bytes = upload.getBytes();

			// ????????? ?????? ??????
			String path = "/ckImage/";// fileDir??? ?????? ????????? ?????? ????????? ?????? ??????????????? ??????.
			String ckUploadPath = path + uid + "_" + fileName;
			File folder = new File(path);

			// ?????? ???????????? ??????
			if (!folder.exists()) {
				try {
					folder.mkdirs(); // ?????? ??????
				} catch (Exception e) {
					e.getStackTrace();
				}
			}

			out = new FileOutputStream(new File(ckUploadPath));
            out.write(bytes);
            out.flush(); // outputStram??? ????????? ???????????? ???????????? ?????????

            String callback = request.getParameter("CKEditorFuncNum");
            printWriter = response.getWriter();
            String fileUrl = "/ckImgSubmit.do?uid=" + uid + "&fileName=" + fileName;  // ????????????

			// ???????????? ????????? ??????
			printWriter.println("{\"filename\" : \"" + fileName + "\", \"uploaded\" : 1, \"url\":\"" + fileUrl + "\"}");
			printWriter.flush();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (printWriter != null) {
					printWriter.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return;
	}

	/**
	 *
	 * <PRE>
	 * 1. MethodName : ckSubmit
	 * 2. ClassName  : PhotoUploadController
	 * 3. Comment   : ckeditor image?????????
	 * 4. ?????????    : :LWJ
	 * 5. ?????????    : 2020. 6. 4. ?????? 3:13:34
	 * </PRE>
	 *   @return void
	 *   @param uid
	 *   @param fileName
	 *   @param request
	 *   @param response
	 *   @throws ServletException
	 *   @throws IOException
	 */
	@RequestMapping(value="/ckImgSubmit.do")
    public void ckSubmit(@RequestParam(value="uid") String uid, @RequestParam(value="fileName") String fileName, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

        //????????? ????????? ????????? ??????
        String path = "/ckImage/";

        String sDirPath = path + uid + "_" + fileName;

        File imgFile = new File(sDirPath);

        //?????? ????????? ?????? ????????? ?????? ??????????????? ??? ????????? ????????? ????????????.
        if(imgFile.isFile()){
            byte[] buf = new byte[1024];
            int readByte = 0;
            int length = 0;
            byte[] imgBuf = null;

            FileInputStream fileInputStream = null;
            ByteArrayOutputStream outputStream = null;
            ServletOutputStream out = null;

            try{
                fileInputStream = new FileInputStream(imgFile);
                outputStream = new ByteArrayOutputStream();
                out = response.getOutputStream();

                while((readByte = fileInputStream.read(buf)) != -1){
                    outputStream.write(buf, 0, readByte);
                }

                imgBuf = outputStream.toByteArray();
                length = imgBuf.length;
                out.write(imgBuf, 0, length);
                out.flush();

            }catch(IOException e){

            }finally {
                outputStream.close();
                fileInputStream.close();
                out.close();
            }
        }
    }

}
