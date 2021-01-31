package com.github.goatgitter.modmanager.gui;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
/******************************************************************************************
 *              MOD TOAST
 * ---------------------------------------------------------------------------------------
 * @author	h1ppychick
 * @since	01/29/2021
 * @purpose	Displays messages to user.  Based off of SystemToast.
 *          Allows mods to add multiple messages before displaying the message.
 *          This is done to prevent multiple messages displaying on top of each other
 *          and blocking the using from being able to read all the messages.
 *          Used during Mod List Import to display errors and successes encountered during
 *          the import process.  
 *          Also adjusts how long messages are displayed to allow time to read lengthy
 *          messages.
 ****************************************************************************************/

public class ModToast implements Toast{
	/***************************************************
	 *              CONSTANTS
	 **************************************************/
	private static final long DEFAULT_WAIT_TIME = 5000L;
	private static final long IMPORT_WAIT_TIME = 10000L;
	private static final long SUCCESS_WAIT_TIME = 2500L;
	public static enum Type {IMPORT,SUCCESS,ERROR;}
	/***************************************************
	 *              INSTANCE VARIABLES
	 **************************************************/
	private final ModToast.Type type;
	private Text title;
	private List<OrderedText> lines = new ArrayList<OrderedText>();
	private long startTime;
	private boolean justUpdated;
	private int width;
	   
	/***************************************************
	 *              CONSTRUCTORS
	 **************************************************/
	public ModToast(ModToast.Type type, Text title, @Nullable Text description) {
		this(type, title, getTextAsList(description), 160);
	}

	public static ModToast create(MinecraftClient client, ModToast.Type type, Text title, Text description) {
		TextRenderer textRenderer = client.textRenderer;
		List<OrderedText> list = textRenderer.wrapLines(description, 200);
		textRenderer.getClass();
		int maxWidth = getMaxWidth(textRenderer, list);
	      
		int i = maxWidth + 30;
		return new ModToast(type, title, list, i);
	}

	private ModToast(ModToast.Type type, Text title, List<OrderedText> lines, int width) {
		this.type = type;
		this.title = title;
		addAllToLines(lines);
		this.width = width;
		}
	/***************************************************
	 *              METHODS
	 **************************************************/
	private void addAllToLines(List<OrderedText> list)
	{
		for(OrderedText o: list)
		{
			lines.add(o);
		}
	}
	   
	private static int getMaxWidth(TextRenderer textRenderer,List<OrderedText> list)
	{
		textRenderer.getClass();
		int maxWidth = 200;
		for(OrderedText o: list)
		{
			int thisWidth = textRenderer.getWidth(o);
			maxWidth = Math.max(maxWidth, thisWidth);
		}
		return maxWidth;
	}
	   
	public void addTextToList(MinecraftClient client,Text description)
	{
		TextRenderer textRenderer = client.textRenderer;
		List<OrderedText> list = textRenderer.wrapLines(description, 200);
		addAllToLines(list);
		int maxWidth = getMaxWidth(textRenderer, lines);
		width = maxWidth + 30;
	}
	   
	private static List<OrderedText> getTextAsList(@Nullable Text text) 
	{
		List<OrderedText> newList = new ArrayList<OrderedText>();
		if (text != null)
		{
			newList.add(text.asOrderedText());
		}
		return newList;
	}

	public int getWidth() 
	{
		return this.width;
	}

	public Toast.Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) 
	{
		if (this.justUpdated) 
		{
			this.startTime = startTime;
			this.justUpdated = false;
		}

		manager.getGame().getTextureManager().bindTexture(TEXTURE);
		RenderSystem.color3f(1.0F, 1.0F, 1.0F);
		int i = this.getWidth();
		int j = 1;
		int o;
		if (i == 160 && this.lines.size() <= 1) 
		{
			manager.drawTexture(matrices, 0, 0, 0, 64, i, this.getHeight());
		} 
		else 
		{
			o = this.getHeight() + Math.max(0, this.lines.size() - 1) * 12;
			int l = 1;
			int m = Math.min(4, o - 28);
			this.drawPart(matrices, manager, i, 0, 0, 28);

			for(int n = 28; n < o - m; n += 10) 
			{
				this.drawPart(matrices, manager, i, 16, n, Math.min(16, o - n - m));
			}

			this.drawPart(matrices, manager, i, 32 - m, o - m, m);
		}

		if (this.lines == null) 
		{
			manager.getGame().textRenderer.draw(matrices, (Text)this.title, 18.0F, 12.0F, -256);
		} 
		else 
		{
			manager.getGame().textRenderer.draw(matrices, (Text)this.title, 18.0F, 7.0F, -256);
			for(o = 0; o < this.lines.size(); ++o) 
			{
				manager.getGame().textRenderer.draw(matrices, (OrderedText)((OrderedText)this.lines.get(o)), 18.0F, (float)(18 + o * 12), -1);
			}
		}
		long waitTime = DEFAULT_WAIT_TIME;
	    switch(type)
	    {
	    	case IMPORT:
	    		waitTime = IMPORT_WAIT_TIME;
	    		break;
	    	case SUCCESS:
	    		waitTime = SUCCESS_WAIT_TIME;
	    		break;
	    	default:
	    		waitTime = DEFAULT_WAIT_TIME;
	    		break;
	    }
		return startTime - this.startTime <  waitTime ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
	}

	private void drawPart(MatrixStack matrices, ToastManager manager, int width, int textureV, int y, int height) 
	{
		int i = textureV == 0 ? 20 : 5;
		int j = Math.min(60, width - i);
		manager.drawTexture(matrices, 0, y, 0, 64 + textureV, i, height);

		for(int k = i; k < width - j; k += 64) 
		{
			manager.drawTexture(matrices, k, y, 32, 64 + textureV, Math.min(64, width - k - j), height);
		}
		manager.drawTexture(matrices, width - j, y, 160 - j, 64 + textureV, j, height);
	}

	public void setContent(Text title, @Nullable Text description) 
	{
		this.title = title;
		this.lines = getTextAsList(description);
		this.justUpdated = true;
	}

	public ModToast.Type getType() 
	{
		return this.type;
	}

	public static void add(ToastManager manager, ModToast.Type type, Text title, @Nullable Text description) 
	{
		manager.add(new ModToast(type, title, description));
	}

	public static void show(ToastManager manager, ModToast.Type type, Text title, @Nullable Text description) 
	{
		ModToast systemToast = (ModToast)manager.getToast(ModToast.class, type);
		if (systemToast == null) 
		{
			add(manager, type, title, description);
		} 
		else 
		{
			systemToast.setContent(title, description);
		}

	}
}
