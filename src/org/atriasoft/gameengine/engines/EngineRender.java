package org.atriasoft.gameengine.engines;

import java.util.Vector;

import org.atriasoft.gameengine.internal.Log;
import org.atriasoft.gameengine.Component;
import org.atriasoft.gameengine.Engine;
import org.atriasoft.gameengine.Environement;
import org.atriasoft.gameengine.camera.Camera;
import org.atriasoft.gameengine.components.ComponentAI;
import org.atriasoft.gameengine.components.ComponentRender;

class ResultNearestElement {
	public ComponentRender element;
	public float dist;
};

public class EngineRender extends Engine {
	public static final String ENGINE_NAME = "render";
	private float accumulator = 0;
	private static float TIME_STEP = 5.0f;
	private Vector<ComponentRender> components = new Vector<ComponentRender>();
	private Vector<ResultNearestElement> displayElementOrdered = new Vector<ResultNearestElement>();
	//private ResourceColored3DObject debugDrawProperty;
	public EngineRender(Environement env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void componentRemove(Component ref) {
		components.remove(ref);
	}

	@Override
	public void componentAdd(Component ref) {
		if (ref instanceof ComponentRender == false) {
			return;
		}
		components.add((ComponentRender)ref);
	}

	@Override
	public void update(long deltaMili) {
		// Add the time difference in the accumulator
		accumulator += (float)deltaMili*0.0001f;
		// While there is enough accumulated time to take one or several physics steps
		while (accumulator >= TIME_STEP) {
			// Log.warning("RENDER: Generate for " + accumulator + " / " + TIME_STEP + "  for:" + components.size());
			// call every object to usdate their constant forces applyed
			for (ComponentRender it: components) {
				it.update(TIME_STEP);
			}
			// Decrease the accumulated time
			accumulator -= TIME_STEP;
		}
		
	}

	@Override
	public void render(long deltaMili, Camera camera) {
		Log.info("Render ...");
		//Matrix4f tmpMatrix;
		for (ComponentRender it: this.components) {
			//Log.info("Render " + it);
			it.render();
		}
		/*
		getOrderedElementForDisplay(this.displayElementOrdered, camera->getEye(), camera->getViewVector());
		Log.verbose("DRAW : " + this.displayElementOrdered.size() + "/" + this.component.size() + " elements");
		
		// note :  the first pass is done at the reverse way to prevent multiple display od the same point in the screen 
		//         (and we remember that the first pass is to display all the non transparent elements)
		for (int32t iii=this.displayElementOrdered.size()-1; iii >= 0; iii--) {
			this.displayElementOrdered[iii].element->draw(0);
		}
		// for the other pass the user can draw transparent elements ...
		for (int32t pass=1; pass <= NUMBEROFSUBPASS+1; pass++) {
			for (auto &it: this.displayElementOrdered) {
				it.element->draw(pass);
			}
		}
		*/
		
	}

//	@Override
//	public void renderDebug(long deltaMili, Camera camera) {
//		//Log.debug("Draw (start)");
//		Matrix4f tmpMatrix;
//		getOrderedElementForDisplay(this.displayElementOrdered, camera->getEye(), camera->getViewVector());
//		Log.verbose("DRAW : " + this.displayElementOrdered.size() + "/" + this.component.size() + " elements");
////		if (propertyDebugPhysic.get() == true) {
////			// Draw debug ... (Object)
////			for (int32t iii=this.displayElementOrdered.size()-1; iii >= 0; iii--) {
////				this.displayElementOrdered[iii].element->drawDebug(this.debugDrawProperty, camera);
////			}
////			// Draw debug ... (Camera)
////			/*
////			etk::Map<etk::String, ege::Camera>> listCamera = this.env->getCameraList();
////			for (auto &itCam : listCamera) {
////				if (itCam.second != null) {
////					itCam.second->drawDebug(this.debugDrawProperty, camera);
////				}
////			}
////			*/
////		}
//		if (propertyDebugNormal.get() == true) {
//			// Draw debug ... (Object)
//			for (int32t iii=this.displayElementOrdered.size()-1; iii >= 0; iii--) {
//				this.displayElementOrdered[iii].element.drawNormalDebug(this.debugDrawProperty);
//			}
//		}
////		if (propertyDebugApplication.get() == true) {
////			// Draw debug ... (User)
////			signalDisplayDebug.emit(this.debugDrawProperty);
////		}
////		/* TODO set it back ...
////		if (camera != null) {
////			this.env->getParticuleEngine().draw(*camera);
////		}
////		*/
//		
//	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return ENGINE_NAME;
	}

	@Override
	public void renderDebug(long deltaMili, Camera camera) {
		// TODO Auto-generated method stub
		
	}

}
//	//ResourceColored3DObject debugDrawProperty;
//	void getOrderedElementForDisplay(etk::Vector<ege::render::Engine::ResultNearestElement>& resultList,
//	                                 const vec3& position,
//	                                 const vec3& direction) {
//		// TODO Set it back ... corrected...
//		// remove all unneeded elements (old display...)
//		resultList.clear();
//		// basic element result
//		ege::render::Engine::ResultNearestElement result;
//		result.dist = 99999999999.0f;
//		result.element = null;
//		// for all element in the game we chek if it is needed to display it ...
//		for (ComponentRender it: this.components) {
//			// check null  pointer
//			if (it == null) {
//				// no pointer null are set in the output list ...
//				continue;
//			}
//			result.element = it;
//			// check distance ...
//			vec3 destPosition = result.element->getTransform().getPosition();
//			vec3 angleView = (destPosition - position);
//			angleView.safeNormalize();
//			float dotResult = angleView.dot(direction);
//			//Log.debug("Dot position : " + destPosition + "  == > dot=" + dotResult);
//			/*
//			if (dotResult <= 0.85f) {
//				// they are not in the camera angle view ...  == > no need to process display
//				continue;
//			}
//			*/
//			result.dist = (position - destPosition).length();
//			/*
//			if (result.dist>500.0f) {
//				// The element is realy too far ...  == > no need to display
//				continue;
//			}
//			*/
//			// try to add the element at the best positions:
//			sizet jjj;
//			for (jjj=0; jjj<resultList.size(); jjj++) {
//				if (resultList[jjj].dist>result.dist) {
//					resultList.insert(resultList.begin()+jjj, result);
//					break;
//				}
//			}
//			// add element at the end :
//			if (jjj >= resultList.size()) {
//				resultList.pushBack(result);
//			}
//		}
//	}
