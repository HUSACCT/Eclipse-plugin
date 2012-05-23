package plugin.controller;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JInternalFrame;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.custom.BusyIndicator;

import plugin.views.internalframes.JInternalHusacctViolationsFrame;

import husacct.Main;
import husacct.ServiceProvider;
import husacct.common.dto.ModuleDTO;
import husacct.common.dto.ViolationDTO;
import husacct.control.ControlServiceImpl;
import husacct.control.task.MainController;
import husacct.control.task.StateController;
import husacct.control.task.WorkspaceController;

public class PluginController {
	private static PluginController pluginController = null;
	
	private ServiceProvider serviceProvider;
	private StateController stateController;
 	private ControlServiceImpl controlService;	
 	private MainController mainController;
 	private WorkspaceController workspaceController;
	private JInternalFrame JInternalFrameValidate, JInternalFrameDefine, JInternalFrameAnalysedGraphics, JInternalFrameDefinedGraphics, JInternalFrameAnalyse;
 	private JInternalHusacctViolationsFrame JInternalViolationsFrame;
	private Logger logger = Logger.getLogger(PluginController.class);;
 	private IProject project;
 	private String projectName = "";
 	private IPath projectPath;
 	private File file;
 	
 	private PluginController(){ 
 		URL propertiesFile = getClass().getResource("/husacct/common/resources/husacct.properties");
		PropertyConfigurator.configure(propertiesFile);
		initializeControllers();	
		initializeFrames();
 	}
 	
 	public static PluginController getInstance(){
 		if(pluginController == null){
 			pluginController = new PluginController(); 
 		}
		return pluginController;
 	}
 	private void initializeControllers(){
 		logger.info("Initializing Controllers");
 		serviceProvider = ServiceProvider.getInstance(); 
 		controlService = (ControlServiceImpl) serviceProvider.getControlService();
 		mainController = controlService.getMainController();
 		stateController = mainController.getStateController();
 		workspaceController = mainController.getWorkspaceController();
 	} 	
 	
 	private void initializeFrames(){
 		logger.info("Initializing Frames");
 		JInternalFrameValidate = serviceProvider.getValidateService().getBrowseViolationsGUI();
		JInternalFrameValidate.setVisible(true);
		
		JInternalFrameDefine = serviceProvider.getDefineService().getDefinedGUI();
		JInternalFrameDefine.setVisible(true);
		
		JInternalFrameAnalysedGraphics = serviceProvider.getGraphicsService().getAnalysedArchitectureGUI();
		JInternalFrameAnalysedGraphics.setVisible(true);
		
		JInternalFrameDefinedGraphics = serviceProvider.getGraphicsService().getDefinedArchitectureGUI();
		JInternalFrameDefinedGraphics.setVisible(true);
		
		JInternalFrameAnalyse = serviceProvider.getAnalyseService().getJInternalFrame();
		JInternalFrameAnalyse.setVisible(true);		
 	}
 	
 	public StateController getStateController(){
 		return stateController;
 	}
 	
 	public JInternalFrame getDefineFrame(){
 		return JInternalFrameDefine;
 	}
 	
 	public JInternalFrame getValidateFrame(){
 		return JInternalFrameValidate;
 	}
 	
 	public JInternalFrame getGraphicsAnalysedArchitecture(){
 		return JInternalFrameAnalysedGraphics;
 	}
 	
 	public JInternalFrame getGraphicsDefinedArchitecture(){
 		return JInternalFrameDefinedGraphics;
 	}
 	
 	public JInternalFrame getAnalyseFrame(){
 		return JInternalFrameAnalyse;
 	}
 	
	public void setViolationFrame(JInternalHusacctViolationsFrame violationFrame){
		JInternalViolationsFrame = violationFrame;
	}
 	
 	public void validate(){
 		if(serviceProvider.getDefineService().isMapped()){	
 			Thread validateThread = new Thread(){
 				 public void run() {
 					ServiceProvider.getInstance().getValidateService().checkConformance();			 
 				 }
 			};
 			BusyIndicator.showWhile(null, validateThread);
 			validateThread.run();
 		}
 	}
 	
 	public void projectSelected(IProject project){
 		this.project = project;
 		projectPath = project.getLocation();
		projectName =  project.toString().substring(2);
		File newFile = new File(projectPath.toString() + "\\" + "hussact.hu");
 		if(workspaceController.isOpenWorkspace()){
 			if(!file.toString().equals(newFile.toString())){
 				saveProject();
 			}
		}
 		else if(newFile.exists()){ 			
 			loadProject();
 		}	
 		else{
 			workspaceController.createWorkspace(projectName);
 			serviceProvider.getDefineService().createApplication(projectName, new String[]{projectPath.toString()}, "Java", "1.0");
 		}
 		analyse();
		stateController.checkState();
 	}
 	
 	private void saveProject(){
 		logger.debug("saving project");
 		HashMap<String, Object> dataValues = new HashMap<String, Object>();
		dataValues.put("file", file);
		workspaceController.saveWorkspace("xml", dataValues);
 	}
 	
 	private void loadProject(){
 		logger.debug("loading project");
 		HashMap<String, Object> dataValues = new HashMap<String, Object>();
		dataValues.put("file", file);
		workspaceController.loadWorkspace("xml", dataValues);
 	}
 	
 	public IProject getProject(){
 		return project;
 	}
 	
 	public String getProjectName(){
 		return projectName;
 	}
 	
 	public String getProjectPath(){
 		return projectPath.toString();
 	}
	
	public void analyse(){
		Thread analyseThread = new Thread(){
			 public void run() {
				 ServiceProvider.getInstance().getAnalyseService().analyseApplication();				 
			 }
		};
		BusyIndicator.showWhile(null, analyseThread);
		analyseThread.run();
	}
	
	public ArrayList<ViolationDTO> getViolations(){
		ArrayList<ViolationDTO> violationArrayList = new ArrayList<ViolationDTO>();
		ModuleDTO[] moduleList;
		moduleList = serviceProvider.getDefineService().getRootModules();
		
		//Setting all violation per module in a ArrayList
		for(ModuleDTO mdtoFrom : moduleList){
			for(ModuleDTO mdtoTo: moduleList){
				if(mdtoFrom != mdtoTo){
					violationArrayList.addAll(Arrays.asList(serviceProvider.getValidateService().getViolationsByLogicalPath(mdtoFrom.logicalPath, mdtoTo.logicalPath)));
				}
			}
		}
		return violationArrayList;
	}
	
	public void resetPlugin(){
		serviceProvider.resetServices();
		//reset views?
	}
	
	public Object[][] setDataModel(){
		ArrayList<ViolationDTO> violationArrayList = pluginController.getViolations();
		Object[][] data = new Object[][]{ { "", "", "", ""} };
		
		if(violationArrayList.size() > 1){
			data = new Object[violationArrayList.size()][4];
		
			int counter = 0;
			for(ViolationDTO violationDTO : violationArrayList){
				data[counter][0] = violationDTO.fromClasspath;
				data[counter][1] = violationDTO.toClasspath;
				data[counter][2] = "" + violationDTO.linenumber;
				data[counter][3] = violationDTO.violationType.getKey();
				counter++;
			}
		}
		return data;
	}
	
	public void refreshViolationFrame(){
		validate();
		JInternalViolationsFrame.initiateViolationTable();
	}

}
