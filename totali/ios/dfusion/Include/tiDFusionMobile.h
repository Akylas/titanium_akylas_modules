/**
   *
   * Copyright (c) 2010, 2011, 2012 TOTAL IMMERSION, All Rights Reserved
   * No part of this software and its documentation may be used, copied,
   * modified, distributed and transmitted, in any form or by any means,
   * without the prior written permission of TOTAL IMMERSION
   *
   * TOTAL IMMERSION, 22 rue Edouard Nieuport 92150 SURESNES FRANCE
   * contact@t-immersion.com www.t-immersion.com
   *
   */
  
 #import <Foundation/Foundation.h>
 #import <UIKit/UIKit.h>
 
 // for info only : NSString* TI_COMPONENT_INTERFACE_VERSION_INFO__INTERNAL = @"3.25.24177.Release.Regular";
 
 /** 
  * @brief D'Fusion Mobile SDK Component interface 
  *
  * interface for the D'Fusion Mobile Component initialization and usage
  * 
  */
 @interface tiComponent : NSObject
 
 /**
  *	@brief 'setRendererType' method indicates to D'Fusion the renderer to be used. This has to be coherent 
  *  with the renderer that your scenario is supposed to use.
  *	@param	rendererType : a string identifier of the renderer to be used
  *  @see TI_RENDERER_GLES1
  *  @see TI_RENDERER_GLES2
  */
 - (void)setRendererType:(NSString*)rendererType;
  
 /**
  *	@brief 'initialize' method instantiate and open the D'Fusion Mobile component
  *	@param	renderView : the UIView in which DFusion Mobile will do its rendering
  *	@return	 the id of the instantiated tiDFusionMobile object
  */
 - (id)initialize:(UIView*)renderView;
  
 /**
  *	@brief 'initialize' method instantiate and open the D'Fusion Mobile component
  *	@param	renderView : the UIView in which DFusion Mobile will do its rendering
  *	@param	orientation : the required orientation for DFusion rendering, only LandscapeRight and Portrait are available
  *	@return	 the id of the instantiated tiDFusionMobile object
  */
 - (id)initialize:(UIView*)renderView withOrientation:(UIInterfaceOrientation) orientation;
  
 /**
  *	@brief 'terminate' method will close the DFusion mobile component
  *	@return	TRUE if it has been correctly closed, FALSE otherwise
  */
 - (BOOL)terminate;
  
 /**
  *	@brief 'isInitialized' method returns the D'Fusion Mobile Component actual status
  *	@return	TRUE if the component is initialized
  *	@return TRUE if the command was executed successfully, FALSE otherwise  
  */
 - (BOOL)isInitialized;
  
 /**
  *	@brief 'unloadScenario' method makes the D'Fusion Mobile component unload the actually executed scenario
  *	@return TRUE if the command was executed successfully, FALSE otherwise  
  */
 - (BOOL)unloadScenario;
  
 /**
  *	@brief 'loadScenario' method makes the D'Fusion Mobile component load and process the filename project
  *	@param	dpdFilename : the project path that is to be loaded
  *	@return TRUE if the command was executed successfully, FALSE otherwise  
  */
 - (BOOL)loadScenario:(NSString*)dpdFilename;
  
 /**
  *	@brief 'playScenario' method that effectively run and show the scenario
  *	@return TRUE if the command was executed successfully, FALSE otherwise  
  */
 - (BOOL)playScenario;
  
 /**
  *	@brief 'pauseScenario' method suspends the scenario play
  *	@return TRUE if the command was executed successfully, FALSE otherwise  
  */
 - (BOOL)pauseScenario;
  
 /**
  *	@brief 'isScenarioPaused' questions the actual state of the scenario play
  *	@return TRUE if the scenario play is paused, FALSE otherwise  
  */
 - (BOOL)isScenarioPaused;
  
 /**
  *	@brief 'registerCommunicationCallback' the notification to be posted with the 'callbackObject' id when the methodName is called from lua
  *	@param	callbackStringIdentifier : the string that Lua script will use for call
  *	@param	callbackObject : an object id that will be associated to the Notification
  *	@param	callbackMethod : a notification name that will be posted in ObjC when Lua will call the 'callbackStringIdentifier'
  *	@return TRUE if the command was executed successfully, FALSE otherwise 
  */
 - (BOOL)registerCommunicationCallback:(NSString*)callbackStringIdentifier callbackObject:(id)callbackObject callbackMethod:(NSString*)callbackMethod;
  
 /**
  *	@brief 'enqueueCommand' method pushed a command with some args for execution into the communication pipe to lua script
  *	@param	commandName : the string command that must be sent to Lua script
  *	@param	args : an array of NSString for the command arguments
  *	@return TRUE if the command was executed successfully, FALSE otherwise
  */
 - (BOOL)enqueueCommand:(NSString*)commandName args:(NSArray*)args;
  
 /**
  *	@brief 'registerNotificationLicense' method registers a user- the scenario play
  *	@param	callbackObject : an object id that will be associated to the Notification
  *	@param	callbackMethod : the string that Lua script can call
  *	@return TRUE if the command was executed successfully, FALSE otherwise  
  */
 - (BOOL)registerNotificationLicense:(id)callbackObject callbackMethod:(SEL)callbackMethod;
  
 /**
  *	@brief 'setCustom' method sets custom values
  *	@param	value : custom string
  */
 - (void)setCustom:(NSString*)value;
  
 /**
  *	@brief 'postRequest' method posts requests
  *	@param	value : string identifier of the request
  *	@param	args : an array of NSString for the request arguments
  */
 - (void)postRequest:(NSString*)req args:(NSArray*)args;
 
 /**
  *	@brief 'setCustomUserAppDataDirectory' method permits to choose a different location for the UserAppDataDirectory than the default behavior exposes.
  *	@param	dirPath : string identifier of the request
  *	@return TRUE if the command was executed successfully, FALSE otherwise  
  */
 + (BOOL)setCustomUserAppDataDirectory:(NSString*)dirPath;
 
 + (NSString*) REQ__SET_IOS_DISPLAY_LINK_FRAME_INTERVAL; // has one NSString* argument - the string representation of the value of the FrameInterval for DisplayLink 
 + (NSString*) REQ__TI_ACTIVATE_RETINA; // no arguments for this request
 
 + (NSString*) TI_RENDERER_GLES1;
 + (NSString*) TI_RENDERER_GLES2;
 
  @end
  	
  
 
