/**
 * @author Edouard DUPIN
 * 
 * @copyright 2011, Edouard DUPIN, all right reserved
 * 
 * @license BSD v3 (see license file)
 */

#include <ege/debug.h>

int32_t ege::getLogId(void) {
	static int32_t g_val = etk::log::registerInstance("ege");
	return g_val;
}

