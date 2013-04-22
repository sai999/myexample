package org.mifosplatform.portfolio.inventory.api;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.infrastructure.core.api.ApiConstants;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.service.FileUtils;
import org.mifosplatform.infrastructure.documentmanagement.command.DocumentCommand;
import org.mifosplatform.portfolio.billingproduct.PortfolioApiDataBillingConversionService;
import org.mifosplatform.portfolio.billingproduct.PortfolioApiJsonBillingSerializerService;
import org.mifosplatform.portfolio.inventory.command.ItemDetailsCommand;
import org.mifosplatform.portfolio.inventory.domain.ItemDetails;
import org.mifosplatform.portfolio.inventory.service.ItemDetailsWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.File;
//////
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;


import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;



@Path("/itemdetails")
@Component
@Scope("singleton")
public class ItemDetailsApiResource {

	@Autowired
	private ItemDetailsWritePlatformService itemDetailsWritePlatformService;
	@Autowired
	private PortfolioApiDataBillingConversionService apiDataConversionService;
	@Autowired
	private PortfolioApiJsonBillingSerializerService apiJsonSerializerService;

	private final ToApiJsonSerializer<ItemDetails> toApiJsonSerializer;
	
	 @Autowired
	    public ItemDetailsApiResource(final ToApiJsonSerializer<ItemDetails> toApiJsonSerializer ) {
	       
	        
	        this.toApiJsonSerializer = toApiJsonSerializer;
	        
	    }

	 @GET
	    @Consumes({ MediaType.APPLICATION_JSON })
	    @Produces({ MediaType.APPLICATION_JSON })
	    public String retrieveData(@Context final UriInfo uriInfo) {

	    // context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

	   // final Collection<ClientUrlData> codes = this.readPlatformService.retrieveAllCodes();

	   //  final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
	     return null;
	    }
	
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response  addItemDetails(final String jsonRequestBody){
	
		final ItemDetailsCommand command = apiDataConversionService.convertJsonToItemDetailsCommand(null, jsonRequestBody);
		CommandProcessingResult id = this.itemDetailsWritePlatformService.addItem(command);
		return Response.ok().entity(id).build();
	}
	
	    @POST
	    @Path("/documents")
	    @Consumes({ MediaType.MULTIPART_FORM_DATA })
	    @Produces({ MediaType.APPLICATION_JSON })
	    public String createUploadFile(
	            @HeaderParam("Content-Length") Long fileSize, @FormDataParam("file") InputStream inputStream,
	            @FormDataParam("file") FormDataContentDisposition fileDetails, @FormDataParam("file") FormDataBodyPart bodyPart,
	            @FormDataParam("name") String name, @FormDataParam("description") String description) {

	        FileUtils.validateFileSizeWithinPermissibleRange(fileSize, name, ApiConstants.MAX_FILE_UPLOAD_SIZE_IN_MB);
	        
	        int i;
	        
	        /*DocumentCommand documentCommand = new DocumentCommand(null, null, null, null, name, fileDetails.getFileName(), fileSize,
	                bodyPart.getMediaType().toString(), description, null);*/
	        try{
	        String fileUploadLocation = FileUtils.generateXlsFileDirectory();
	        String fileName=fileDetails.getFileName();
	        if (!new File(fileUploadLocation).isDirectory()) {
                new File(fileUploadLocation).mkdirs();
            }

            String fileLocation = FileUtils.saveToFileSystem(inputStream, fileUploadLocation,fileName);
            
            InputStream excelFileToRead = new FileInputStream(fileLocation);
        	
	        XSSFWorkbook wb = new XSSFWorkbook(excelFileToRead);

			XSSFSheet sheet = wb.getSheetAt(0);
			XSSFRow row;
			XSSFCell cell;
			String serialno = "0";
			int countno = Integer.parseInt(serialno);
			if (countno == 0) {
				countno = countno + 2;
			} else if (countno == 1) {
				countno = countno + 1;
			}
			System.out.println("Excel Row No is: " + countno);
			Iterator rows = sheet.rowIterator();
			Vector<XSSFCell> v = new Vector<XSSFCell>();
			if (countno > 0) {
				countno = countno - 1;
			}
			while (rows.hasNext()) {

				row = (XSSFRow) rows.next();
				i = row.getRowNum();
				if (i > 0) {
					if (i >= countno) {
						Iterator cells = row.cellIterator();
						while (cells.hasNext()) {

							cell = (XSSFCell) cells.next();

							if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING) {
								// System.out.print(cell.getStringCellValue() +
								// " ");
								v.add(cell);
							} else if (cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
								// System.out.print(cell.getNumericCellValue() +
								// " ");
								v.add(cell);
							} else {
								v.add(cell);
							}

							
							
						}
						//ItemDetailsCommand itemdetails=new ItemDetailsCommand(Integer.parseInt(v.elementAt(0).toString()),v.elementAt(1).toString(),Integer.parseInt(v.elementAt(2).toString()),v.elementAt(3).toString(),v.elementAt(4).toString(),v.elementAt(5).toString(),Integer.parseInt(v.elementAt(6).toString()),Integer.parseInt(v.elementAt(7).toString()),Integer.parseInt(v.elementAt(8).toString()),v.elementAt(9).toString(),null);
						List<ItemDetailsCommand> ItemDetailsCommandList=new ArrayList<ItemDetailsCommand>();
						/*for(i=0;i<10;i++)
						{*/
						/* Iterator iterator = v.iterator();
						 while(iterator.hasNext())
						 {

							System.out.println(v.elementAt(0).toString());
							System.out.println( v.elementAt(1).toString());
							System.out.println( v.elementAt(2).toString());
							System.out.println( v.elementAt(3).toString());
							System.out.println( v.elementAt(4).toString());
							System.out.println( v.elementAt(5).toString());
							System.out.println( v.elementAt(6).toString());
							System.out.println( v.elementAt(7).toString());
							System.out.println( v.elementAt(8).toString());
							System.out.println( v.elementAt(9).toString());
							
							
						
							// itemDetails = ItemDetails.create(command.getItemMasterId(), command.getSerialNumber(), command.getGrnId(),command.getProvisioningSerialNumber(), command.getQuality(),command.getStatus(), command.getOfficeId(), command.getClientId(), command.getWarranty(), command.getRemark());
							//ItemDetailsCommand itemdetails=new ItemDetailsCommand(Long.parseLong(v.elementAt(0).toString()), v.elementAt(1).toString(), Long.parseLong(v.elementAt(2).toString()), v.elementAt(3).toString(), v.elementAt(4).toString(), v.elementAt(5).toString(), Long.parseLong(v.elementAt(6).toString()), Long.parseLong(v.elementAt(7).toString()), Long.parseLong(v.elementAt(8).toString()), v.elementAt(9).toString());
							//ItemDetailsCommand itemdetails=new ItemDetailsCommand(Long.parseLong(v.elementAt(0).toString()), v.elementAt(1).toString(), Long.parseLong(v.elementAt(2).toString()), v.elementAt(3).toString(), v.elementAt(4).toString(), v.elementAt(5).toString(), Long.parseLong(v.elementAt(6).toString()), Long.parseLong(v.elementAt(7).toString()), Long.parseLong(v.elementAt(8).toString()), v.elementAt(9).toString());
							//CommandProcessingResult id = this.itemDetailsWritePlatformService.addItem(itemdetails);
							//ItemDetailsCommandList.add(itemdetails);
							
							ItemDetailsCommand itemDetailsCommand=new ItemDetailsCommand();
							new Double(v.elementAt(0).toString()).longValue();
							
							itemDetailsCommand.setItemMasterId(new Double(v.elementAt(0).toString()).longValue());
							
							itemDetailsCommand.setSerialNumber(v.elementAt(1).toString());
							itemDetailsCommand.setGrnId(new Double(v.elementAt(2).toString()).longValue());
							itemDetailsCommand.setProvisioningSerialNumber( v.elementAt(3).toString());
							itemDetailsCommand.setQuality( v.elementAt(4).toString());
							itemDetailsCommand.setRemark(v.elementAt(9).toString());
							itemDetailsCommand.setStatus(v.elementAt(5).toString());
							itemDetailsCommand.setOfficeId(new Double(v.elementAt(6).toString()).longValue());
							itemDetailsCommand.setClientId(new Double(v.elementAt(7).toString()).longValue());
							itemDetailsCommand.setWarranty(new Double(v.elementAt(8).toString()).longValue());
							ItemDetailsCommandList.add(itemDetailsCommand);
							
							CommandProcessingResult id = this.itemDetailsWritePlatformService.addItem(itemDetailsCommand);	
							
						}
						*/
						
						
						
					}
					
					
				
				
				}
			}
			 Iterator iterator = v.iterator();
			 while(iterator.hasNext())
			 {

				System.out.println(v.elementAt(0).toString());
				System.out.println( v.elementAt(1).toString());
				System.out.println( v.elementAt(2).toString());
				System.out.println( v.elementAt(3).toString());
				System.out.println( v.elementAt(4).toString());
				System.out.println( v.elementAt(5).toString());
				System.out.println( v.elementAt(6).toString());
				System.out.println( v.elementAt(7).toString());
				System.out.println( v.elementAt(8).toString());
				System.out.println( v.elementAt(9).toString());
				
				
			
				// itemDetails = ItemDetails.create(command.getItemMasterId(), command.getSerialNumber(), command.getGrnId(),command.getProvisioningSerialNumber(), command.getQuality(),command.getStatus(), command.getOfficeId(), command.getClientId(), command.getWarranty(), command.getRemark());
				//ItemDetailsCommand itemdetails=new ItemDetailsCommand(Long.parseLong(v.elementAt(0).toString()), v.elementAt(1).toString(), Long.parseLong(v.elementAt(2).toString()), v.elementAt(3).toString(), v.elementAt(4).toString(), v.elementAt(5).toString(), Long.parseLong(v.elementAt(6).toString()), Long.parseLong(v.elementAt(7).toString()), Long.parseLong(v.elementAt(8).toString()), v.elementAt(9).toString());
				//ItemDetailsCommand itemdetails=new ItemDetailsCommand(Long.parseLong(v.elementAt(0).toString()), v.elementAt(1).toString(), Long.parseLong(v.elementAt(2).toString()), v.elementAt(3).toString(), v.elementAt(4).toString(), v.elementAt(5).toString(), Long.parseLong(v.elementAt(6).toString()), Long.parseLong(v.elementAt(7).toString()), Long.parseLong(v.elementAt(8).toString()), v.elementAt(9).toString());
				//CommandProcessingResult id = this.itemDetailsWritePlatformService.addItem(itemdetails);
				//ItemDetailsCommandList.add(itemdetails);
				
				ItemDetailsCommand itemDetailsCommand=new ItemDetailsCommand();
				new Double(v.elementAt(0).toString()).longValue();
				
				itemDetailsCommand.setItemMasterId(new Double(v.elementAt(0).toString()).longValue());
				
				itemDetailsCommand.setSerialNumber(v.elementAt(1).toString());
				itemDetailsCommand.setGrnId(new Double(v.elementAt(2).toString()).longValue());
				itemDetailsCommand.setProvisioningSerialNumber( v.elementAt(3).toString());
				itemDetailsCommand.setQuality( v.elementAt(4).toString());
				itemDetailsCommand.setRemark(v.elementAt(9).toString());
				itemDetailsCommand.setStatus(v.elementAt(5).toString());
				itemDetailsCommand.setOfficeId(new Double(v.elementAt(6).toString()).longValue());
				itemDetailsCommand.setClientId(new Double(v.elementAt(7).toString()).longValue());
				itemDetailsCommand.setWarranty(new Double(v.elementAt(8).toString()).longValue());
				//ItemDetailsCommandList.add(itemDetailsCommand);
				
				CommandProcessingResult id = this.itemDetailsWritePlatformService.addItem(itemDetailsCommand);	
				
			}
			
			
			
			
			
			
	        }catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
	       
	       
	        /**
	         * TODO: also need to have a backup and stop reading from stream after
	         * max size is reached to protect against malicious clients
	         **/

	        /**
	         * TODO: need to extract the actual file type and determine if they are
	         * permissable
	         **/
	        
	        
	       /// ItemDetailsCommand itemDetailsCommand=new ItemDetailsCommand()
	       // DocumentCommand documentCommand = new DocumentCommand(null, null, entityType, entityId, name, fileDetails.getFileName(), fileSize,
	           ///     bodyPart.getMediaType().toString(), description, null);

	       //Long documentId = this.documentWritePlatformService.createDocument(documentCommand, inputStream);

	        //return this.toApiJsonSerializer.serialize(CommandProcessingResult.resourceResult(1, null));
	        return null;
	    }
	
	
}
