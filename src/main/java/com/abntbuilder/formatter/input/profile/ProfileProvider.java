package com.abntbuilder.formatter.input.profile;

import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;

public interface ProfileProvider {

    DocumentProfile findById(String profileId);
}
