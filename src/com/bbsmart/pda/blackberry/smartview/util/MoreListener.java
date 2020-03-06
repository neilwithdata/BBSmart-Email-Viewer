package com.bbsmart.pda.blackberry.smartview.util;

import net.rim.blackberry.api.mail.BodyPart;

public interface MoreListener {
	public void notifySuccess(BodyPart bp);

	public void notifyFailure(BodyPart bp, String errorMessage);
}