package openblocks.client.renderer.block.canvas;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IUnlistedProperty;

public class CanvasState {

	public static final IUnlistedProperty<CanvasState> PROPERTY = new IUnlistedProperty<CanvasState>() {

		@Override
		public String getName() {
			return "sides";
		}

		@Override
		public boolean isValid(CanvasState value) {
			return true;
		}

		@Override
		public Class<CanvasState> getType() {
			return CanvasState.class;
		}

		@Override
		public String valueToString(CanvasState value) {
			return value.toString();
		}
	};

	public static final CanvasState EMPTY = new CanvasState(ImmutableMap.<EnumFacing, CanvasSideState> of());

	public final Map<EnumFacing, CanvasSideState> sideStates;

	private CanvasState(Map<EnumFacing, CanvasSideState> sideStates) {
		this.sideStates = sideStates;
	}

	public CanvasState update(EnumFacing side, CanvasSideState sideState) {
		final CanvasSideState oldSideState = sideStates.get(side);
		if (oldSideState != null) oldSideState.release();

		sideState.acquire();

		final ImmutableMap.Builder<EnumFacing, CanvasSideState> builder = ImmutableMap.builder();

		// put new one first, then rest (without old one) - since order is important
		builder.put(side, sideState);
		for (Map.Entry<EnumFacing, CanvasSideState> e : sideStates.entrySet())
			if (e.getKey() != side)
				builder.put(e);

		return new CanvasState(builder.build());
	}

	public void acquire() {
		for (CanvasSideState side : sideStates.values())
			side.acquire();
	}

	public void release() {
		for (CanvasSideState sideState : sideStates.values())
			sideState.release();
	}

	public void onRender() {
		for (CanvasSideState sideState : sideStates.values())
			sideState.onRender();
	}

	@Override
	public int hashCode() {
		return sideStates.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;

		if (obj instanceof CanvasState) {
			final CanvasState other = (CanvasState)obj;
			return this.sideStates.equals(other.sideStates);
		}

		return false;
	}

	@Override
	public String toString() {
		List<String> elements = Lists.newArrayListWithExpectedSize(6);
		for (Map.Entry<EnumFacing, CanvasSideState> e : sideStates.entrySet())
			elements.add(e.getKey() + ":" + e.getValue());

		return "[" + Joiner.on(", ").join(elements) + "]";
	}

	// LRU collection of paint application
	public Collection<EnumFacing> applicationOrder() {
		return sideStates.keySet();
	}
}
