package edu.kh.deploy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

class HomeControllerTest {
    @Test
    void homeReturnHomeViewAndMessage(){
        ExtendedModelMap model = new ExtendedModelMap();

        String view = new HomeController().home(model);
        assertEquals("home",view);
        assertEquals("hello deploy", model.getAttribute("message"));
    }
    

    
}
