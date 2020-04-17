package excellent.cancer.gray.light.jdbc.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.util.Date;

/**
 * 文档中的一章，是具有具体文件的
 *
 * @author XyParaCrim
 */
@Data
@AllArgsConstructor
@Alias("DocumentChapter")
public class DocumentChapter {

    /**
     * 文档ID
     */
    private Long id;

    /**
     * 创建日期
     */
    private Date createdDate;

    /**
     * 最近修改日期
     */
    private Date updatedDate;

    /**
     * 目录标题
     */
    private String title;

    /**
     * 隶属的目录Id
     */
    private Long catalogId;

    /**
     * 一组对于具体资源的属性
     */
    private boolean isEmpty;

    private String downloadLink;

    private String uploadLink;

}