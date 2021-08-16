import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.thingworx.entities.utils.ThingUtilities;
import com.thingworx.logging.LogUtilities;
import com.thingworx.metadata.FieldDefinition;
import com.thingworx.metadata.annotations.ThingworxBaseTemplateDefinition;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinition;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinitions;
import com.thingworx.metadata.annotations.ThingworxServiceDefinition;
import com.thingworx.metadata.annotations.ThingworxServiceParameter;
import com.thingworx.metadata.annotations.ThingworxServiceResult;
import com.thingworx.things.Thing;
import com.thingworx.things.repository.FileRepositoryThing;
import com.thingworx.types.InfoTable;
import com.thingworx.types.primitives.IPrimitiveType;

@ThingworxBaseTemplateDefinition(name = "GenericThing")
@ThingworxPropertyDefinitions(properties = {
		@ThingworxPropertyDefinition(name = "Image", description = "", category = "", baseType = "IMAGE", isLocalOnly = false, aspects = {
				"isPersistent:true", "dataChangeType:VALUE" }) })
public class ExportToPDFTestThingTemplate extends Thing {

	private static Logger _logger = LogUtilities.getInstance().getApplicationLogger(ExportToPDFTestThingTemplate.class);
	public ExportToPDFTestThingTemplate() {
	}
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	private String date = sdf.format(new Date());
	private FileRepositoryThing DataExporterRepository = (FileRepositoryThing) ThingUtilities
			.findThing("SystemRepository");
	@ThingworxServiceDefinition(name = "ExportToPDFTestService", description = "", category = "", isAllowOverride = false, aspects = {
			"isAsync:false" })
	@ThingworxServiceResult(name = "Result", description = "", baseType = "STRING", aspects = {})
	public String ExportToPDFTestService(
			@ThingworxServiceParameter(name = "infotable", description = "", baseType = "INFOTABLE", aspects = {
					"isEntityDataShape:true" }) InfoTable infotable) throws Exception {
		int columnSize = infotable.getFieldCount();

		final Rectangle pageSize = PageSize.A4.rotate();
		Document document = new Document(pageSize);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		String fileName = "Export" + date + ".pdf";
		
		final PdfWriter writer = PdfWriter.getInstance(document, bos);
		document.open();
		
		byte[] getImagePropertyValue = this.GetImagePropertyValue("Image");
		Image image = Image.getInstance(getImagePropertyValue);
		image.scalePercent(20);
		
		document.add(image);
		
		PdfPTable table = new PdfPTable(columnSize);
		table.setTotalWidth(pageSize.getWidth() - 10);
		table.setLockedWidth(true);
		table.setSplitLate(false);
		Font headerFont = new Font(Font.TIMES_ROMAN, 11, Font.BOLD, new Color(0, 0, 0));
		Font rowFont = new Font(Font.TIMES_ROMAN, 11);

		// create the table header with the field definitions from the input infotable
		infotable.getDataShape().getFields().getOrderedFieldsByOrdinal().forEach(fieldDefinition -> {
			PdfPCell cell = new PdfPCell(new Phrase(fieldDefinition.getName(), headerFont));
			cell.setBackgroundColor(Color.LIGHT_GRAY);
			table.addCell(cell);
		});
		
		//populate the rest of the table with values
        for (int rowIndex = 0; rowIndex < infotable.getRowCount(); rowIndex++) {
            table.completeRow();
            for (FieldDefinition field : infotable.getDataShape().getFields().getOrderedFieldsByOrdinal()) {
                IPrimitiveType cellValue = infotable.getRow(rowIndex).getOrDefault(field.getName(),
                        field.getDefaultValue());
                if (cellValue != null) {
                    PdfPCell cell = new PdfPCell(new Phrase(cellValue.getStringValue()));
                    table.addCell(cell);
                } else {
                    table.addCell("");

                }
            }
        }

		document.add(table);

		document.close();
		DataExporterRepository.CreateBinaryFile(fileName, bos.toByteArray(), true);
		
		_logger.trace("Exiting Service: ExportToPDFService");
		return "Success";
	}
	
}
