package org.proofcafe.android.socketserver.tests

import junit.framework.Assert._
import _root_.android.test.AndroidTestCase

class UnitTests extends AndroidTestCase {
  def testPackageIsCorrect {
    assertEquals("org.proofcafe.android.socketserver", getContext.getPackageName)
  }
}