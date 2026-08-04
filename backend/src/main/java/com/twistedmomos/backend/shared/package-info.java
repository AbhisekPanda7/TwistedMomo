/**
 * Cross-cutting infrastructure every module may use. Open, so its sub-packages
 * stay accessible without each one being an explicit named interface.
 */
@org.springframework.modulith.ApplicationModule(type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package com.twistedmomos.backend.shared;
