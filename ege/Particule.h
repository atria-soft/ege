/**
 * @author Edouard DUPIN
 * 
 * @copyright 2011, Edouard DUPIN, all right reserved
 * 
 * @license BSD v3 (see license file)
 */

#ifndef __EGE_PARTICULE_H__
#define __EGE_PARTICULE_H__

namespace ege {
	class Particule;
};

#include <etk/types.h>
#include <ege/Environement.h>
#include <ege/Camera.h>


namespace ege {
	/**
	 * @brief The particule class is an element with no control, when it will be created,
	 *        it does not have any control, for example smoke or reactor generation ...
	 *        or explosion particule ...
	 */
	class Particule {
		protected:
			ege::ParticuleEngine& m_particuleEngine;
			const char* m_particuleType;
		public:
			/**
			 * @brief Constructor.
			 * @param[in] _particuleEngine reference on the particule engine ...
			 * @param[in] _particuleType Type of the particule (set NULL if you did not want to use the respowner ...)
			 */
			Particule(ege::ParticuleEngine& _particuleEngine, const char* _particuleType = NULL);
			/**
			 * @brief Destructor.
			 */
			virtual ~Particule(void) { };
			/**
			 * @brief init the particule
			 */
			virtual void init(void) { };
			/**
			 * @brief Un-init the particule
			 */
			virtual void UnInit(void) { };
			/**
			 * @brief update the paticule properties
			 * @param[in] _delta Delta time from the previous call
			 */
			virtual void update(float _delta) { };
			/**
			 * @brief draw the current particule
			 */
			virtual void draw(const ege::Camera& _camera) { };
			/**
			 * @brief Check if the element might be removed
			 * @return true : The element might be removed
			 * @return false : The element might be keeped
			 */
			virtual bool needRemove(void) {
				return false;
			};
			/**
			 * @brief get the type of the particule
			 * @return Type of the current particule
			 */
			const char* getParticuleType(void) {
				return m_particuleType;
			};
			/**
			 * @brief When the particule arrive to his end of life, this function is called.
			 */
			virtual void onEnd(void) {};
	};
};

#endif


