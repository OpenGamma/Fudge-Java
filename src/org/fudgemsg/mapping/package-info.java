/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * The core API for converting Java objects to/from Fudge messages.
 * <p>
 * Conversion to and from objects is encoded in a separate layer on top of the wire protocol.
 * This allows the serialization layer to be changed more easily without affecting the wire.
 * <p>
 * This package implements the serialization framework described at
 * <a href="http://wiki.fudgemsg.org/display/FDG/Serialization+Framework">Serialization Framework</a>.
 */
package org.fudgemsg.mapping;
