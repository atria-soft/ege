/**
 * @author Edouard DUPIN
 * 
 * @copyright 2010, Edouard DUPIN, all right reserved
 * 
 * @license APACHE-2 (see license file)
 */
#pragma once

#include <ewol/widget/Windows.h>
#include <ege/Environement.h>
#include <ege/camera/View.h>

namespace appl {
	class Windows : public ewol::widget::Windows {
		private:
			std::shared_ptr<ege::Environement> m_env;
			std::shared_ptr<ege::camera::View> m_camera;
		protected:
			Windows();
			void init();
		public:
			DECLARE_FACTORY(Windows);
			virtual ~Windows() { };
		private:
			void onCallbackPeriodicUpdateCamera(const ewol::event::Time& _event);
	};
}

