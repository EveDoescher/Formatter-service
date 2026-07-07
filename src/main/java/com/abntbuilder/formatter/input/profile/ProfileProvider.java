package com.abntbuilder.formatter.input.profile;

import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;

import java.util.Collection;

public interface ProfileProvider {

    DocumentProfile findById(String profileId);

    Collection<DocumentProfile> allProfiles();
}
