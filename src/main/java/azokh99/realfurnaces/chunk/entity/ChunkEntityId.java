package azokh99.realfurnaces.chunk.entity;

public class ChunkEntityId {

    private int id;

    public ChunkEntityId() {
        this.id = -1;
    }

    public ChunkEntityId(int id) {
        this.id = id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ChunkEntityId other = (ChunkEntityId) obj;
        if (id != other.id)
            return false;
        return true;
    }

}
