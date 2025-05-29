package JP2ImageConverter.kakadu;

import kdu_jni.Kdu_dims;

public class CompressLocalService {

    public void kdc_file_binding(char string, int len, kdu_long offset) {
      /* `len' is the length of the name string, while `offset' is the
         number of leading bytes in the file to skip over. */
        fname = new char[len+1]; fname[len] = '\0';
        strncpy(fname,string,(size_t) len);
        num_components = first_comp_idx = 0;
        this->offset = offset;
        next = null;
    }

    // Destroys the entire list for convenience.
    public void kdc_file_binding_destroy() {
        delete[] fname;
        if (reader.exists()) {
            reader.destroy();
        }
        if (next != null) {
            delete next;
        }
        public: // Data -- all data public in this local object.
        char *fname;
        int num_components, first_comp_idx;
        Kdu_dims cropping;
        // no java equivalent for kdu_image_in
        kdu_image_in reader;
        // no java equivalent for kdu_long
        kdu_long offset;
        kdc_file_binding *next;
    };
}
