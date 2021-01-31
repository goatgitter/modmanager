package com.github.goatgitter.modmanager.util;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
/**
 * 
 * @author GoatGitter
 * @since 08/11/2020
 * 
 */
public class Stencil {
	/***************************************************
	 *              CONSTANTS
	 **************************************************/
	private static final float ZERO = 0.0F;
	private static final float ONE = 1.0F;
	private static final float HALF = 0.5F;
	public static enum COLOR {BLACK,GREEN, BLUE, WHITE, GREY};
	/***************************************************
	 *              INSTANCE VARIABLES
	 **************************************************/
	private static COLOR drawColor = COLOR.BLACK;
	/***************************************************
	 *              METHODS
	 **************************************************/
	public static void setColorBlack()
	{
		setColor(COLOR.BLACK);
	}
	
	public static void setColorGreen()
	{
		setColor(COLOR.GREEN);
	}
	
	public static void setColorBlue()
	{
		setColor(COLOR.BLUE);
	}
	
	public static void setColorWhite()
	{
		setColor(COLOR.WHITE);
	}
	
	public static void setColorGrey()
	{
		setColor(COLOR.GREY);
	}
	
	private static void setColor(COLOR theColor)
	{
		drawColor = theColor;
	}
	/***************************************************
	 *              RENDERING
	 **************************************************/
	
	private static void setDrawColor()
	{
		switch(drawColor)
		{
			case GREEN:
				RenderSystem.color4f(ZERO,ONE,ZERO,ONE);
				break;
			case BLACK:
				RenderSystem.color4f(ZERO, ZERO, ZERO, ONE);
				break;
			case BLUE:
				RenderSystem.color4f(ZERO,ZERO,ONE,ONE);
				break;
			case WHITE:
				RenderSystem.color4f(ONE, ONE, ONE, ONE);
				break;
			case GREY:
				RenderSystem.color4f(HALF, HALF, HALF, HALF);
				break;
			default:
				RenderSystem.color4f(ZERO, ZERO, ZERO, ONE);
				break;
		}
	}
	
	public static void rectangle(MatrixStack matrices, int leftX, int rightX, int topY, int bottomY)
	{
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		Matrix4f matrix = matrices.peek().getModel();
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		// Paint a rectangle for given coordinates.
		setDrawColor();
		
		buffer.begin(7, VertexFormats.POSITION);
		buffer.vertex(matrix, leftX, bottomY, ZERO).next();
		buffer.vertex(matrix, rightX, bottomY, ZERO).next();
		buffer.vertex(matrix, rightX, topY, ZERO).next();
		buffer.vertex(matrix, leftX, topY, ZERO).next();
		tessellator.draw();
		RenderSystem.disableBlend();
		RenderSystem.enableTexture();
	}
	
	public static void texturedRectangle(MatrixStack matrices, int leftX, int rightX, int topY, int bottomY, int regionLeft, int regionTop, int regionWidth, int regionHeight, int totalWidth, int totalHeight) {
		RenderSystem.enableBlend();		
		DrawableHelper.fill(matrices, leftX, topY, rightX,bottomY, 0xA0909090);
		setColorWhite();
		setDrawColor();
		DrawableHelper.drawTexture(matrices, leftX, topY, regionLeft, regionTop, regionWidth, regionHeight, totalWidth, totalHeight);
		RenderSystem.disableBlend();
	}
}
