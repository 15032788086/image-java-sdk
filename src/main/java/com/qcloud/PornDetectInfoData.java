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
public class PornDetectInfoData {
        public int result;
        public double confidence;
        public double pornScore;
        public double normalScore;
        public double hotScore;
        public int forbidStatus;
    
    	public PornDetectInfoData() {
		result = 0;
		confidence = 0;
		pornScore = 0;
		normalScore = 0;
		hotScore = 0;
                forbidStatus = 0;
	}

	public void print() {
		System.out.println("result = " + result);
		System.out.println("confidence = " + confidence);
		System.out.println("pornScore = " + pornScore);
		System.out.println("normalScore = " + normalScore);
		System.out.println("hotScore = " + hotScore);
                System.out.println("forbidStatus = " + forbidStatus);
	}
}
