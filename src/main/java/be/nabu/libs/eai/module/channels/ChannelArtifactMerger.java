package be.nabu.libs.eai.module.channels;

import javafx.scene.layout.AnchorPane;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactMerger;
import be.nabu.eai.repository.api.Repository;

public class ChannelArtifactMerger implements ArtifactMerger<ChannelArtifact> {

	@Override
	public boolean merge(ChannelArtifact source, ChannelArtifact target, AnchorPane pane, Repository targetRepository) {
		// the target configuration wins
		if (target != null) {
			source.mergeConfiguration(target.getConfig(), true);
		}
		new ChannelArtifactGUIManager().display(MainController.getInstance(), pane, source);
		return true;
	}

	@Override
	public Class<ChannelArtifact> getArtifactClass() {
		return ChannelArtifact.class;
	}
}
