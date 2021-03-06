/** @file
 * @author Edouard DUPIN
 * @copyright 2011, Edouard DUPIN, all right reserved
 * @license MPL v2.0 (see license file)
 */
#pragma once

#include <ege/debug.hpp>

#include <ege/Component.hpp>

namespace ege {
	namespace ia {
		class Component : public ege::Component {
			protected:
				
			public:
				virtual const etk::String& getType() const override;
				// call of this function every time the call will be done
				virtual void update(float _delta);
		};
	}
}