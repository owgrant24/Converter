package com.github.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class CheckStatusServiceTest {

    @Test
    void checkStatus() {
        String status = "frame= 0 fps=0.0 q=-1.0 size= 0kB" +
                "video:2159333kB audio:75148kB subtitle:0kB other streams:0kB global headers:0kB muxing overhead: 0.183652%";
        String statusAfterCheck = CheckStatusService.checkStatus(status);
        assertEquals("Done", statusAfterCheck);
    }

}