/*
 * Util - Utility
 * 
 * Copyright (C) 2011  Antonio Lotito <lotito@ismb.it>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package eu.codlab.nfc.acr122;
public class Util {
        /**
         * Converts a byte array to readable string
         *
         * @param a
         *            array to print
         * @return readable byte array string
         */
        public static String byteArrayToString(byte[] a) {
                if (a == null)
                        return "[null]";
                if (a.length == 0)
                        return "[empty]";
                String result = "";
                String onebyte = null;
                for (int i = 0; i < a.length; i++) {
                        onebyte = Integer.toHexString(a[i]);
                        if (onebyte.length() == 1)
                                onebyte = "0" + onebyte;
                        else
                                onebyte = onebyte.substring(onebyte.length() - 2);
                        result = result + "0x" + onebyte.toUpperCase() + " ";
                }
                return result;
        }

        /**
         * Prints the "raw" APDUs for debugging purposes
         *
         * @param c
         *            Command APDU
         * @param r
         *            Response APDU
         */
        public static void debugAPDUs(byte[] c, byte[] r) {
                if (c != null)
                        System.out.println("[DEBUG] {sending   [" + c.length + " bytes]} "
                                        + byteArrayToString(c));
                if (r != null)
                        System.out.println("[DEBUG] {receiving [" + r.length + " bytes]} "
                                        + byteArrayToString(r));
        }

        /**
         * Append a byte array to another byte array
         *
         * @param first
         *            the byte array to append to
         * @param second
         *            the byte array to append
         * @return the appended array
         */
        public static byte[] appendToByteArray(byte[] first, byte[] second) {
                int secondLength = (second != null) ? second.length : 0;
                return appendToByteArray(first, second, 0, secondLength);
        }

        /**
         * Append a byte array to another byte array specifying which part of the
         * second byte array should be appended to the first
         *
         * @param first
         *            the byte array to append to
         * @param second
         *            the byte array to append
         * @param offset
         *            offset in second array to start appending from
         * @param length
         *            number of bytes to append from second to first array
         * @return the appended array
         */
        public static byte[] appendToByteArray(byte[] first, byte[] second,
                        int offset, int length) {
                if (second == null || second.length == 0) {
                        return first;
                }
                int firstLength = (first != null) ? first.length : 0;

                if (length < 0 || offset < 0 || second.length < length + offset)
                        throw new ArrayIndexOutOfBoundsException();
                byte[] result = new byte[firstLength + length];
                if (firstLength > 0)
                        System.arraycopy(first, 0, result, 0, firstLength);
                System.arraycopy(second, offset, result, firstLength, length);
                return result;
        }

        /**
         * Return a specific part of a byte array starting from <em>offset</em> with
         * <em>length</em>
         *
         * @param array
         *            the byte array
         * @param offset
         *            the offset in the array from where to start in bytes
         * @param length
         *            the number of bytes to get
         * @return the sub byte array
         */
        public static byte[] subByteArray(byte[] array, int offset, int length) {
                return appendToByteArray(null, array, offset, length);
        }
}


