/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qcloud;

/**
 *
 * @author jusisli
 */
public class PornDetectInfo {
        public int code;
        public String message;
        public String name;
        public PornDetectInfoData data;
    
    	public PornDetectInfo() {
		code = 0;
		message = null;
		name = null;
		data = new PornDetectInfoData();
	}

	public void print() {
		System.out.println("code = " + code);
		System.out.println("message = " + message);
		System.out.println("name = " + name);
                System.out.println("data = {");
		data.print();
                System.out.println("}");
	}
}
