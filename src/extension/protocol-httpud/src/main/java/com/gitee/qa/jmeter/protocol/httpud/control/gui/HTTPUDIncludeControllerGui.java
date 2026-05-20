package com.gitee.qa.jmeter.protocol.httpud.control.gui;

import com.gitee.qa.jmeter.protocol.httpud.control.HTTUDIncludeController;
import org.apache.jmeter.control.gui.IncludeControllerGui;
import org.apache.jmeter.testelement.TestElement;

public class HTTPUDIncludeControllerGui extends IncludeControllerGui {

    public String getStaticLabel(){
        return "HTTP User Defined Include控制器"; // $NON-NLS-1$
    }

    public TestElement createTestElement() {
        HTTUDIncludeController mc = new HTTUDIncludeController();
        configureTestElement(mc);
        return mc;
    }
}
