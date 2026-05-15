package com.gitee.qa.jmeter.extractor;

import org.apache.jmeter.processor.gui.AbstractPostProcessorGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledChoice;

import javax.swing.*;
import java.awt.*;

public class ResponseAutoExtractorGui extends AbstractPostProcessorGui {

	private static final long serialVersionUID = 1L;
	
	private JTextField prefix;  // 变量名前缀 默认 "hs_"
	
	private JTextField delimiter;  // 键值分隔符 默认 ", "
	
	private JLabeledChoice showRowsChoice;  // 是否只处理首行数据 默认 "否"

	public ResponseAutoExtractorGui() {
		super();
        init();
	}
	
	@Override
	public String getLabelResource() {
		return null;
	}
	
	@Override
	public String getStaticLabel() {
		return "应答自动提取器";
	}

	@Override
	public TestElement createTestElement() {
		ResponseAutoExtractor extractor = new ResponseAutoExtractor();
        modifyTestElement(extractor);
        return extractor;
	}

	@Override
	public void modifyTestElement(TestElement extractor) {
		super.configureTestElement(extractor);
		if (extractor instanceof ResponseAutoExtractor) {
		    ResponseAutoExtractor respExtractor = (ResponseAutoExtractor) extractor;
		    respExtractor.setFrefixProperty(prefix.getText());
		    respExtractor.setDelimiterProperty(delimiter.getText());
		    boolean showFirst = (showRowsChoice.getText().trim().equals("是")) ?  true : false;
		    respExtractor.setFirstRowProperty(showFirst);
        }
	}
	
	@Override
	public void configure(TestElement extractor) {
	    super.configure(extractor);
	    if (extractor instanceof ResponseAutoExtractor) {
	        ResponseAutoExtractor respExtractor = (ResponseAutoExtractor) extractor;
	        prefix.setText(respExtractor.getFrefixProperty());
	        delimiter.setText(respExtractor.getDelimiterProperty());
	        String showFirst = (respExtractor.getFirstRowProperty()) ? "是" : "否";
	        showRowsChoice.setText(showFirst);
	    }
	}
	
	@Override
    public void clearGui() {
        super.clearGui();
    }
	
	private void init() {
        setLayout(new BorderLayout());
        setBorder(makeBorder());

        Box box = Box.createVerticalBox();
        box.add(makeTitlePanel());
        //box.add(createScopePanel(true));
        box.add(makeParameterCompoment());
        add(box, BorderLayout.NORTH);
    }

	private Box makeParameterCompoment() {
	    Box box = Box.createVerticalBox();
        JPanel panel_1 = new JPanel(new BorderLayout(5, 0));
        panel_1.add(new JLabel("变量名前缀: "), BorderLayout.WEST);
        prefix = new JTextField("hs_");
        panel_1.add(prefix, BorderLayout.CENTER);
        JPanel panel_2 = new JPanel(new BorderLayout(5, 0));
        panel_2.add(new JLabel("键值分隔符: "), BorderLayout.WEST);
        delimiter = new JTextField(", ");
        panel_2.add(delimiter, BorderLayout.CENTER);
        showRowsChoice = new JLabeledChoice("只处理应答首行: ", new String[] {"否", "是"});
        JPanel panel_3 = new JPanel(new BorderLayout(5, 0));
        panel_3.add(showRowsChoice, BorderLayout.WEST);
        box.add(panel_1);
        box.add(panel_2);
        box.add(panel_3);
	    return box;
	}
	
}
